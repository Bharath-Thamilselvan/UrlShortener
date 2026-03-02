package com.example.UrlShortner.domain.repositories;

import com.example.UrlShortner.domain.entities.ShortUrl;
import com.example.UrlShortner.domain.models.PagedResult;
import io.micrometer.observation.ObservationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl,Long> {

    @Query("select su from ShortUrl su left join fetch su.createdBy where su.isPrivate=false")
    Page<ShortUrl> findAllPublicShortUrls(Pageable pageable);

    boolean existsByShortKey(String shortKey);

    Optional<ShortUrl> findByShortKey(String shortKey);

    Page<ShortUrl> findByCreatedById(Long currentUserId, Pageable pageable);

    @Modifying
    void deleteByIdInAndCreatedById(List<Long> ids, Long userId);

    @Query("select su from ShortUrl su left join fetch su.createdBy")
    Page<ShortUrl> findAllUrls(Pageable pageable);
}
