package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfNoColoredManaSpentEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a Void Mirror-style already-qualified counter trigger. */
@Component
@RequiredArgsConstructor
public class CounterSpellIfNoColoredManaSpentEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellIfNoColoredManaSpentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry != null) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
        }
    }
}
