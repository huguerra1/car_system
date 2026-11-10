package com.car_system.gerenciador_carros;

public class CarroNaoEncontradoException extends RuntimeException {
    public CarroNaoEncontradoException(Long id) {
        super("Carro com id " + id + " não encontrado");
    }
}
