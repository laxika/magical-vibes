package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
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
        // The spell's mana value and the mana spent to cast it as the ability resolves, as it last existed on the stack
        // once it has left (CR 608.2h): the mana spent is what the spell recorded as it became cast.
        AmountContext context = AmountContext.forStackEntry(entry, null);
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, new TargetSpellManaValue(), context)
                - amountEvaluationService.evaluate(gameData, new ManaSpentToCast(), context));
        if (amount > 0) {
            lifeSupport.applyLifeLoss(gameData, targetPlayerId, amount, entry.getCard().getName());
        }
    }
}
