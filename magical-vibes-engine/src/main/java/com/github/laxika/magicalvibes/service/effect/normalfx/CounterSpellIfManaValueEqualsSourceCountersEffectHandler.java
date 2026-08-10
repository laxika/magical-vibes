package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsSourceCountersEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the already-qualified Chalice of the Void-style counter trigger. */
@Component
@RequiredArgsConstructor
public class CounterSpellIfManaValueEqualsSourceCountersEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellIfManaValueEqualsSourceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        CounterSpellIfManaValueEqualsSourceCountersEffect counterEffect =
                (CounterSpellIfManaValueEqualsSourceCountersEffect) effect;
        int targetManaValue = targetEntry.getCard().getManaValue() + targetEntry.getXValue();
        if (targetManaValue == counterEffect.manaValueAtTrigger()) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
        }
    }
}
