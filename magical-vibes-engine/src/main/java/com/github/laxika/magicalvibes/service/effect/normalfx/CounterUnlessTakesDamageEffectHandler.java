package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessTakesDamageEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a counterspell that offers the target spell's controller damage instead of countering.
 */
@Component
@RequiredArgsConstructor
public class CounterUnlessTakesDamageEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessTakesDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CounterUnlessTakesDamageEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        String prompt = "Have " + entry.getCard().getName() + " deal " + e.damage()
                + " damage to you to prevent " + targetEntry.getCard().getName() + " from being countered?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetEntry.getControllerId(), List.of(e), prompt,
                targetCardId, entry.getControllerId()));
    }
}
