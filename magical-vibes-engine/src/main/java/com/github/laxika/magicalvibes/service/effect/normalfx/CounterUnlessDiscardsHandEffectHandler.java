package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsHandEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves "counter target spell unless its controller discards their hand". */
@Component
@RequiredArgsConstructor
public class CounterUnlessDiscardsHandEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessDiscardsHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            return;
        }

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) {
            return;
        }

        String prompt = "Discard your hand to prevent " + targetEntry.getCard().getName() + " from being countered?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetEntry.getControllerId(),
                List.of(new CounterUnlessDiscardsHandEffect()), prompt, targetCardId));
    }
}
