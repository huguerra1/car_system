package com.car_system.gerenciador_carros;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CarroEntrada(
        @NotBlank(message = "marca é obrigatória")
        @Size(max = 60, message = "marca deve ter no máximo 60 caracteres")
        String marca,
        @NotBlank(message = "modelo é obrigatório")
        @Size(max = 60, message = "modelo deve ter no máximo 60 caracteres")
        String modelo,
        @NotNull(message = "ano é obrigatório")
        @AnoFabricacaoValido
        Integer ano) {
}
