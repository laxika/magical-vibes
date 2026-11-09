package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves life loss equal to the triggering spell's mana value minus the mana spent to cast it. */
@Component
@RequiredArgsConstructor
public class LoseLifeEqualToTriggeringSpellManaValueDifferenceEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final AmountEvaluationService amountEvaluationService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        // The spell's mana value as the ability resolves, as it last existed on the stack once it has left (CR 608.2h);
        // the mana spent to cast it is the ability's X, recorded as the ability triggered.
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, new TargetSpellManaValue(),
                AmountContext.forStackEntry(entry, null)) - entry.getXValue());
        if (amount > 0) {
            lifeSupport.applyLifeLoss(gameData, targetPlayerId, amount, entry.getCard().getName());
        }
    }
}
