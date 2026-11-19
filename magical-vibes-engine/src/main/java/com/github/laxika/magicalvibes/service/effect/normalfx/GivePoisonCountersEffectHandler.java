package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link GivePoisonCountersEffect}: evaluates the amount once in the entry's context, asks
 * {@link SubjectResolutionService} for the players and gives each of them the counters through
 * {@link LifeSupport#applyPoisonCounters}, so poison suppression (Melira, {@code PlayerCantGetPoisonCountersEffect}),
 * counter replacements and the "you put counters" triggers apply alike.
 */
@Component
@RequiredArgsConstructor
public class GivePoisonCountersEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GivePoisonCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GivePoisonCountersEffect) effect;
        String sourceName = entry.getCard().getName();
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, null));
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, e.player())) {
            lifeSupport.applyPoisonCounters(gameData, playerId, amount, sourceName, entry.getControllerId());
        }
    }
}
