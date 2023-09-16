package com.springboot.shootformoney.bet.repository;

import com.springboot.shootformoney.bet.entity.EuroPool;
import com.springboot.shootformoney.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EuroPoolRepository extends JpaRepository<EuroPool, Long> {
    Boolean existsByGame_gNo(Long gNo);
    EuroPool findByGame_gNo(Long gNo);
}
