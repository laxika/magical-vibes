package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterEachTargetSpellEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CounterEachTargetSpellEffect}: counters every spell chosen for this effect's target
 * group (Double Negative's "counter up to two target spells"). Each target is looked up and countered
 * independently — a target that has left the stack or become uncounterable is simply skipped
 * ({@link CounterSupport#findCounterTarget}), so the other target is still countered (CR 608.2b/608.2c).
 */
@Component
@RequiredArgsConstructor
public class CounterEachTargetSpellEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterEachTargetSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID targetCardId : entry.targetsForEffect(effect)) {
            StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
            if (targetEntry == null) continue;
            counterSupport.counterSpell(gameData, entry, targetEntry);
        }
    }
}
