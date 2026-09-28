package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveRadCountersEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResolveRadCountersEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ResolveRadCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        int radCounters = gameData.playerRadCounters.getOrDefault(playerId, 0);
        if (radCounters <= 0) {
            return;
        }

        List<Card> milledCards = graveyardService.resolveMillPlayer(gameData, playerId, radCounters);
        int nonlandCards = (int) milledCards.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .count();
        if (nonlandCards <= 0) {
            return;
        }

        lifeSupport.applyLifeLoss(gameData, playerId, nonlandCards, "rad counters");
        int remainingRadCounters = Math.max(0,
                gameData.playerRadCounters.getOrDefault(playerId, 0) - nonlandCards);
        gameData.playerRadCounters.put(playerId, remainingRadCounters);
    }
}
