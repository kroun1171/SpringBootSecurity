package com.example.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String Secret_Key="mbsUu8NsRg9J5ZgpTNpysuR1vyI9HgsDYKt/hImYgsHKSIpL5VhiYGOcL14K0ImC";
    public String extractUserName(String token) {
             return extractClaim(token,Claims::getSubject);
    }
    public <T> T extractClaim(String token , Function<Claims,T> claimsResolver){
        final Claims claims =extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public Claims extractAllClaims(String token){
        return Jwts.
                parserBuilder().
                setSigningKey(getSignInKey()).
                build().
                parseClaimsJws(token).
                getBody();
    }

    public boolean isTokenValid(String token,UserDetails userdetails){
        final String username=extractUserName(token);
        return (username.equals(userdetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token,Claims::getExpiration);
    }

    public String generateToken(UserDetails userdetails){
        return generateToken(new HashMap<>(),userdetails);
    }

    public String generateToken(Map<String,Object> extraClaims,
                                UserDetails userDetails){
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode((Secret_Key));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
