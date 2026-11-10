package com.car_system.gerenciador_carros;

import java.time.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AnoFabricacaoValidator implements ConstraintValidator<AnoFabricacaoValido, Integer> {
    @Override
    public boolean isValid(Integer ano, ConstraintValidatorContext context) {
        if (ano == null) {
            return true;
        }
        int limite = Year.now(context.getClockProvider().getClock()).getValue() + 1;
        if (ano >= 1886 && ano <= limite) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("ano deve estar entre 1886 e " + limite)
                .addConstraintViolation();
        return false;
    }
}
