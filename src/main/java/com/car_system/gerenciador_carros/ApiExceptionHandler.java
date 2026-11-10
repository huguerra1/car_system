package com.car_system.gerenciador_carros;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(CarroNaoEncontradoException.class)
    public ResponseEntity<ErroApi> naoEncontrado(CarroNaoEncontradoException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroApi(404, exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApi> validacao(MethodArgumentNotValidException exception) {
        Map<String, List<String>> erros = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(erro ->
                erros.computeIfAbsent(erro.getField(), campo -> new ArrayList<>())
                        .add(erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroApi(400, "Dados inválidos", erros));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApi> corpoInvalido() {
        return ResponseEntity.badRequest()
                .body(new ErroApi(400, "Corpo JSON ausente ou inválido", Map.of()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroApi> parametroInvalido(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(new ErroApi(400, "Parâmetro inválido",
                Map.of(exception.getName(), List.of("deve ser um número inteiro válido"))));
    }

    public record ErroApi(int status, String mensagem, Map<String, List<String>> erros) {
    }
}
