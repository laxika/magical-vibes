package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PutCounterOnCombatOpponentEffect}: puts the configured counters on the referenced
 * combat opponent (carried as the stack entry's non-targeting target) right away, provided it is
 * still a creature on the battlefield. Mindbender Spores.
 */
@Component
@RequiredArgsConstructor
public class PutCounterOnCombatOpponentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnCombatOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnCombatOpponentEffect counterEffect = (PutCounterOnCombatOpponentEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null || counterEffect.amount() <= 0) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        int placed = permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, target, counterEffect.counterType(), counterEffect.amount());
        if (placed <= 0) {
            return;
        }
        if (counterEffect.grantedStaticEffect() != null
                && !target.getPersistentTriggeredEffects(EffectSlot.STATIC)
                        .contains(counterEffect.grantedStaticEffect())) {
            target.addPersistentTriggeredEffect(
                    EffectSlot.STATIC,
                    counterEffect.grantedStaticEffect());
        }
        if (counterEffect.grantedUpkeepEffect() != null
                && !target.getPersistentTriggeredEffects(EffectSlot.UPKEEP_TRIGGERED)
                        .contains(counterEffect.grantedUpkeepEffect())) {
            target.addPersistentTriggeredEffect(
                    EffectSlot.UPKEEP_TRIGGERED,
                    counterEffect.grantedUpkeepEffect());
        }
    }
}
