package br.com.julia.using_spring_security_jwt.dtos;

public class TokenResponse {
    private String token;

    public TokenResponse() {}
    public TokenResponse(String token) { this.token = token; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
