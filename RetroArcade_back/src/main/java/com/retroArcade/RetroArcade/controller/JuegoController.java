package com.retroArcade.RetroArcade.controller;

import com.retroArcade.RetroArcade.model.Juego;
import com.retroArcade.RetroArcade.repository.JuegoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/juegos")
@CrossOrigin(origins = "http://localhost:5500") // Cambiá el puerto según tu frontend
public class JuegoController {

    private final JuegoRepository juegoRepository;

    public JuegoController(JuegoRepository juegoRepository) {
        this.juegoRepository = juegoRepository;
    }

    // 🔹 Obtener un juego por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Juego> getJuegoById(@PathVariable Long id) {
        return juegoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Listar todos los juegos paginados (general)
    @GetMapping
    public ResponseEntity<Page<Juego>> getAllJuegos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Juego> juegos = juegoRepository.findAll(pageable);
        return ResponseEntity.ok(juegos);
    }

    // 🔹 Listar juegos por categoría ID (paginado)
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<Page<Juego>> getJuegosByCategoriaId(
            @PathVariable Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Juego> juegos = juegoRepository.findByCategoriaId(categoriaId, pageable);
        return ResponseEntity.ok(juegos);
    }

    // 🔹 Listar juegos por nombre de categoría (paginado)
    @GetMapping("/categoria/nombre/{nombre}")
    public ResponseEntity<Page<Juego>> getJuegosByCategoriaNombre(
            @PathVariable String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Juego> juegos = juegoRepository.findByCategoriaNombreIgnoreCase(nombre, pageable);
        return ResponseEntity.ok(juegos);
    }

    // 🔹 Agregar un nuevo juego
    @PostMapping
    public Juego addJuego(@RequestBody Juego juego) {
        return juegoRepository.save(juego);
    }

    // 🔹 Obtener juegos aleatorios evitando cargar toda la tabla
    @GetMapping({"/random", "/random/{size}"})
    public List<Juego> getRandomJuegos(@PathVariable(required = false) Integer size) {
        int requestedSize = size == null ? 20 : size;
        int targetSize = Math.min(Math.max(requestedSize, 1), 100);

        long total = juegoRepository.count();
        if (total == 0) {
            return List.of();
        }

        long maxId = juegoRepository.findMaxId();
        if (maxId <= 0) {
            return List.of();
        }

        LinkedHashMap<Long, Juego> seleccionados = new LinkedHashMap<>();
        int attempts = 0;
        int maxAttempts = 8;

        while (seleccionados.size() < targetSize && attempts < maxAttempts) {
            attempts++;

            int missing = targetSize - seleccionados.size();
            int candidateCount = Math.min(Math.max(missing * 3, 30), 300);
            Set<Long> randomIds = new HashSet<>(candidateCount);

            for (int i = 0; i < candidateCount; i++) {
                long randomId = ThreadLocalRandom.current().nextLong(1, maxId + 1);
                randomIds.add(randomId);
            }

            List<Juego> encontrados = juegoRepository.findByIdIn(randomIds);
            Collections.shuffle(encontrados);
            for (Juego juego : encontrados) {
                seleccionados.putIfAbsent(juego.getId(), juego);
                if (seleccionados.size() >= targetSize) {
                    break;
                }
            }
        }

        if (seleccionados.size() < targetSize) {
            int fallbackSize = targetSize - seleccionados.size();
            int totalPages = (int) Math.ceil((double) total / fallbackSize);
            int randomPage = ThreadLocalRandom.current().nextInt(totalPages);

            Pageable pageable = PageRequest.of(randomPage, fallbackSize);
            List<Juego> fallback = new ArrayList<>(juegoRepository.findAll(pageable).getContent());
            Collections.shuffle(fallback);
            for (Juego juego : fallback) {
                seleccionados.putIfAbsent(juego.getId(), juego);
                if (seleccionados.size() >= targetSize) {
                    break;
                }
            }
        }

        List<Juego> result = new ArrayList<>(seleccionados.values());
        Collections.shuffle(result);
        if (result.size() > targetSize) {
            return result.subList(0, targetSize);
        }
        return result;
    }

    @GetMapping("/buscar")
    public Page<Juego> buscarJuegos(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return juegoRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }

}
