# Using Spring Security JWT API

API REST com autenticação stateless usando Spring Security 6 + JWT, documentação via OpenAPI/Swagger e banco em memória H2.

Principais tecnologias: Spring Boot 3, Spring Security, JPA/Hibernate, H2, JJWT (io.jsonwebtoken), springdoc-openapi.

URL padrão de execução: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

H2 Console: http://localhost:8080/h2-console

## Visão geral da segurança (JWT)

- Autenticação via token JWT no header Authorization com prefixo configurável (padrão: `Bearer`).
- Sessão stateless (sem sessão no servidor); cada requisição deve enviar o token.
- Claims principais do token: `sub` (usuário), `iat` (emissão), `exp` (expiração) e `authorities` (roles do usuário).
- Assinatura HS256 usando chave derivada da propriedade `security.config.key`.
- Filtro `JWTFilter` valida o token em todas as requisições protegidas e popula o SecurityContext.

Propriedades (arquivo `src/main/resources/application.properties`):

- `security.config.prefix` → Prefixo do header (ex.: `Bearer`).
- `security.config.key` → Segredo para assinar o token (ex.: `SECRET_KEY`).
- `security.config.expiration` → Expiração do token em milissegundos (ex.: `3600000` = 1h).

## Fluxo de autenticação

1) Criar usuário (público): `POST /users` com nome, username, senha e lista de roles.

2) Login (público): `POST /auth/login` com `username` e `password`. Se válido, retorna `{ token: "Bearer <jwt>" }`.

3) Acessar endpoints protegidos enviando `Authorization: Bearer <jwt>`.

Usuário seed: ao subir a aplicação, é criado automaticamente o usuário `admin` (senha `admin`) com role `ROLE_ADMIN`.

## Endpoints

Autorização global (em `WebSecurityConfig`):

- Público: `POST /auth/login`, `POST /users`, `/h2-console/**`, `/v3/api-docs/**`, `/swagger-ui/**`.
- Protegido: qualquer outro endpoint (exige JWT válido).

### POST /users (público)

Cria um usuário. A senha é criptografada com BCrypt antes de salvar.

Body JSON esperado:

```
{
	"name": "Julia Moraes",
	"username": "julia",
	"password": "minhasenha",
	"roles": ["ROLE_USER"]
}
```

Observações sobre roles:
- Podem ser enviados com ou sem o prefixo `ROLE_`. O token sempre normaliza para `ROLE_...`.
- As roles são usadas como authorities do Spring Security.

Respostas:
- 200/201 em caso de sucesso (sem body).
- 400 se `username` já existir.

### POST /auth/login (público)

Autentica e retorna o token JWT com prefixo.

Body JSON:

```
{
	"username": "maria",
	"password": "minhasenha"
}
```

Resposta 200:

```
{
	"token": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Erros:
- 401 quando as credenciais são inválidas.

### GET /users/me (protegido)

Retorna o username do usuário autenticado.

Header:
- `Authorization: Bearer <jwt>`

Resposta 200 (text/plain):
- `"maria"`

Erros comuns:
- 401 sem enviar Authorization.
- 403 token inválido/expirado (exceções de parsing de JWT no filtro).

## Modelos (DTOs)

- `LoginRequest`: `{ username: string, password: string }`
- `TokenResponse`: `{ token: string }`
- `User` (entidade): `{ id?: number, name: string, username: string, password: string, roles: string[] }`

## Execução local

Pré-requisitos: Java 17+ e Maven.

No Windows PowerShell:

```powershell
./mvnw.cmd spring-boot:run
```

Aplicação sobe em http://localhost:8080.

## Teste rápido (PowerShell)

1) Criar um usuário:

```powershell
$body = @{ name = 'Maria'; username = 'maria'; password = '123456'; roles = @('ROLE_USER') } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/users' -ContentType 'application/json' -Body $body
```

2) Fazer login e capturar o token:

```powershell
$login = @{ username = 'maria'; password = '123456' } | ConvertTo-Json
$resp = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/auth/login' -ContentType 'application/json' -Body $login
$token = $resp.token
$token
```

3) Chamar endpoint protegido com o token:

```powershell
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/users/me' -Headers @{ Authorization = $token }
```

## Banco de dados

- H2 em memória: `jdbc:h2:mem:testdb` (credenciais padrão `sa`/`sa`).
- Console H2 habilitado em `/h2-console`.
- Entidades: `User` e coleção `tab_user_roles` (roles por usuário).

## Personalização

Ajuste as propriedades em `application.properties`:

```
security.config.prefix=Bearer
security.config.key=SECRET_KEY
security.config.expiration=3600000
```

- Mude `key` para um segredo forte.
- Ajuste `expiration` conforme necessidade.

## Notas de implementação

- `JWTFilter` lê o header Authorization, valida e define a autenticação no contexto.
- `JWTCreator` assina e valida tokens; normaliza roles com prefixo `ROLE_`.
- `UserDetailsService` carrega o usuário do banco e converte roles em `SimpleGrantedAuthority`.
- `UserService` aplica BCrypt na senha e evita duplicidade de username.
- `DataInitializer` cria `admin/admin` com `ROLE_ADMIN` no start.

