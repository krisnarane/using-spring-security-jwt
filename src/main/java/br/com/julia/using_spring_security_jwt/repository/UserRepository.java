package br.com.julia.using_spring_security_jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.julia.using_spring_security_jwt.model.User;


public interface UserRepository extends JpaRepository<User, Integer>{
    // la busca um usuário (User) e carrega junto os papéis (roles) usando JOIN FETCH, filtrando pelo nome de usuário.
    // jpql
    @Query("SELECT e FROM User e JOIN FETCH e.roles WHERE e.username= (:username)")
    // metodos de busca
    public User findByUsername(@Param("username") String username);

    boolean existsByUsername(String username);
    
} 
