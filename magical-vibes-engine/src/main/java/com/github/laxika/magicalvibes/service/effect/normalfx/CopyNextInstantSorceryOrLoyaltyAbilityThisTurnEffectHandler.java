package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Component
public class CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffectHandler implements NormalEffectHandlerBean {

    private static final int COPIES_PER_EVENT = 2;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.pendingNextInstantSorceryCopyThisTurnCount.merge(controllerId, COPIES_PER_EVENT, Integer::sum);
        gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.merge(controllerId, COPIES_PER_EVENT, Integer::sum);
        for (int i = 0; i < COPIES_PER_EVENT; i++) {
            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn
                    .computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                    .add(new GameData.PendingCopySource(entry.getCard(), entry.getSourcePermanentId()));
        }
        gameData.pendingNextSpellOrLoyaltyCopyLinkedThisTurn.add(controllerId);
        log.info("Game {} - {} will copy their next instant, sorcery, or loyalty ability this turn",
                gameData.id, controllerId);
    }
}
