package com.retroArcade.RetroArcade.repository;

import com.retroArcade.RetroArcade.model.Juego;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long> {
    Page<Juego> findByCategoriaNombreIgnoreCase(String nombre, Pageable pageable);
    Page<Juego> findByCategoriaId(Long categoriaId, Pageable pageable);

    Page<Juego> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    List<Juego> findByIdIn(Collection<Long> ids);

    @Query("select coalesce(max(j.id), 0) from Juego j")
    long findMaxId();
}
