package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RadCounterEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RadCounterEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;

    public RadCounterEffectHandler(GraveyardService graveyardService, LifeSupport lifeSupport,
                                   GameQueryService gameQueryService) {
        this.graveyardService = graveyardService;
        this.lifeSupport = lifeSupport;
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RadCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int radCounters = gameData.playerRadCounters.getOrDefault(entry.getControllerId(), 0);
        if (radCounters <= 0) {
            return;
        }

        var milledCards = graveyardService.resolveMillPlayerIncludingExiled(
                gameData, entry.getControllerId(), radCounters);
        int nonlandCards = (int) milledCards.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .count();
        if (nonlandCards > 0) {
            if (gameQueryService.radiationLifeLossBecomesLifeGain(gameData, entry.getControllerId())) {
                lifeSupport.applyGainLife(gameData, entry.getControllerId(), nonlandCards, "rad counters");
            } else {
                // CR 728.1: the loss follows the mill. As a step of its own, a Lich's Duel Mastery sacrifice replacing it
                // asks its CR 616.1 questions without milling again.
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                        List.of(new PlayersLoseLifeEffect(Map.of(entry.getControllerId(), nonlandCards))));
            }
            gameData.playerRadCounters.computeIfPresent(entry.getControllerId(),
                    (ignored, current) -> Math.max(0, current - nonlandCards));
        }
    }
}
