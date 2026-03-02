package com.example.UrlShortner.domain.services;

import com.example.UrlShortner.ApplicationProperties;
import com.example.UrlShortner.domain.entities.ShortUrl;
import com.example.UrlShortner.domain.models.CreateShortUrlCmd;
import com.example.UrlShortner.domain.models.PagedResult;
import com.example.UrlShortner.domain.models.ShortUrlDto;
import com.example.UrlShortner.domain.repositories.ShortUrlRepository;
import com.example.UrlShortner.domain.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;

    private final UserRepository userRepository;

    private final EntityMapper entityMapper;

    private final ApplicationProperties properties;

    public ShortUrlService(ShortUrlRepository shortUrlRepository,EntityMapper entityMapper,
                           ApplicationProperties properties,UserRepository userrepository){
        this.shortUrlRepository=shortUrlRepository;
        this.entityMapper=entityMapper;
        this.properties=properties;
        this.userRepository=userrepository;
    }
    public PagedResult<ShortUrlDto> findAllPublicShortUrls(int pageNo, int pageSize) {
        Pageable pageable=getPageable(pageNo,pageSize);
        Page<ShortUrlDto> shortUrlDtoPage=shortUrlRepository.findAllPublicShortUrls(pageable)
                .map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlDtoPage);
    }

    private Pageable getPageable(int pageNo,int pageSize){
        pageNo=pageNo>1?pageNo-1:0;
        return PageRequest.of(pageNo,pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
    }

    public PagedResult<ShortUrlDto> getUserShortUrls(Long currentUserId, int pageNo, int pageSize) {
        Pageable pageable=getPageable(pageNo,pageSize);
        Page<ShortUrlDto> shortUrlDtoPage=shortUrlRepository.findByCreatedById(currentUserId,pageable)
                .map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlDtoPage);
    }

    @Transactional
    public void deleteUserShortUrls(List<Long> ids, Long userId) {
        if(ids!=null && !ids.isEmpty() && userId!=null){
            shortUrlRepository.deleteByIdInAndCreatedById(ids,userId);
        }
    }

    @Transactional
    public ShortUrlDto createShortUrl(CreateShortUrlCmd cmd) {
        if(properties.validateOriginalUrl()){
            boolean urlExists=UrlExistenceValidator.isUrlExists(cmd.originalUrl());
            if(!urlExists){
                throw new RuntimeException("Invalid Url"+cmd.originalUrl());
            }
        }
        var shortKey=generateUniqueKey();
        var shortUrl=new ShortUrl();
        shortUrl.setOriginalUrl(cmd.originalUrl());
        shortUrl.setShortKey(shortKey);

        if(cmd.userId()==null){
            shortUrl.setCreatedBy(null);
            shortUrl.setIsPrivate(false);
            shortUrl.setExpiresAt(Instant.now().plus(properties
                    .defaultExpiryInDays(), ChronoUnit.DAYS));
        }
        else{
            shortUrl.setCreatedBy(userRepository.findById(cmd.userId()).orElseThrow());
            shortUrl.setIsPrivate(cmd.isPrivate()!=null && cmd.isPrivate());
            shortUrl.setExpiresAt(cmd.expirationInDays()!=null?Instant.now().plus(cmd.expirationInDays()
                    , ChronoUnit.DAYS):null);
        }

        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(Instant.now());
        shortUrlRepository.save(shortUrl);
        return entityMapper.toShortUrlDto(shortUrl);
    }

    private String generateUniqueKey(){
        String shortKey;
        do{
            shortKey=generateRandomKey();
        }while (shortUrlRepository.existsByShortKey(shortKey));
        return shortKey;
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String  generateRandomKey(){
        StringBuilder sb=new StringBuilder(SHORT_KEY_LENGTH);
        for(int i=0;i<SHORT_KEY_LENGTH;i++){
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Transactional
    public Optional<ShortUrlDto> accessShortUrl(String shortKey,Long userId) {
        Optional<ShortUrl> shortUrlOptional=shortUrlRepository.findByShortKey(shortKey);
        if(shortUrlOptional.isEmpty()){
            return Optional.empty();
        }
        ShortUrl shortUrl=shortUrlOptional.get();
        if(shortUrl.getExpiresAt()!=null && shortUrl.getExpiresAt().isBefore(Instant.now())){
            return Optional.empty();
        }
        if(shortUrl.getIsPrivate()!=null && shortUrl.getIsPrivate() && shortUrl.getCreatedBy()!=null &&
                !Objects.equals(shortUrl.getCreatedBy().getId(), userId)
        ){
            return Optional.empty();
        }
        shortUrl.setClickCount(shortUrl.getClickCount()+1);
        return  shortUrlOptional.map(entityMapper::toShortUrlDto);
    }

    public PagedResult<ShortUrlDto> findAllShortUrls(int pageNo, int pageSize) {
        Pageable pageable =getPageable(pageNo,pageSize);
        Page<ShortUrlDto> shortUrlDtoPage=shortUrlRepository.findAllUrls(pageable)
                .map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlDtoPage);
    }
}
