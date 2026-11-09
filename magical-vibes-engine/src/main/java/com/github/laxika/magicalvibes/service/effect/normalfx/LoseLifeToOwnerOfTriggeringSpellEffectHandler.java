package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOwnerOfTriggeringSpellEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves life loss by the owner of the spell that caused the trigger. */
@Component
@RequiredArgsConstructor
public class LoseLifeToOwnerOfTriggeringSpellEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeToOwnerOfTriggeringSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card triggeringSpell = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringSpell == null || triggeringSpell.getOwnerId() == null) {
            return;
        }

        UUID ownerId = triggeringSpell.getOwnerId();
        // "Equal to that spell's mana value": as it last existed on the stack once it has left (CR 608.2h).
        lifeSupport.applyLifeLoss(gameData, ownerId, amountEvaluationService.evaluate(gameData, new TargetSpellManaValue(),
                AmountContext.forStackEntry(entry, null)), entry.getCard().getName());
    }
}
