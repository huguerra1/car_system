package com.car_system.gerenciador_carros;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarroService {
    private final CarroRepository repository;

    public List<Carro> listar() {
        return repository.findAll(Sort.by("id"));
    }

    public Carro buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new CarroNaoEncontradoException(id));
    }

    @Transactional
    public Carro criar(CarroEntrada entrada) {
        Carro carro = new Carro();
        preencher(carro, entrada);
        return repository.save(carro);
    }

    @Transactional
    public Carro atualizar(Long id, CarroEntrada entrada) {
        Carro carro = buscar(id);
        preencher(carro, entrada);
        return repository.save(carro);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    private void preencher(Carro carro, CarroEntrada entrada) {
        carro.setMarca(entrada.marca());
        carro.setModelo(entrada.modelo());
        carro.setAno(entrada.ano());
    }
}
