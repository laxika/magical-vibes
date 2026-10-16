package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextLoyaltyAbilityThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
public class CopyNextLoyaltyAbilityThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextLoyaltyAbilityThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.merge(entry.getControllerId(), 1, Integer::sum);
        gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> new ArrayList<>())
                .add(new GameData.PendingCopySource(entry.getCard(), entry.getSourcePermanentId()));
        log.info("Game {} - {} will copy their next loyalty ability this turn",
                gameData.id, entry.getControllerId());
    }
}
