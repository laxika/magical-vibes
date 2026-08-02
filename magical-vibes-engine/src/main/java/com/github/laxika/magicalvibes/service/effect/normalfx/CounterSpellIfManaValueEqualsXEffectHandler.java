package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsXEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CounterSpellIfManaValueEqualsXEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellIfManaValueEqualsXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        int targetManaValue = targetEntry.getCard().getManaValue() + targetEntry.getXValue();
        if (targetManaValue == entry.getXValue()) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
        } else {
            log.info("Game {} - {} has mana value {}, discarded card's mana value was {}, spell not countered",
                    gameData.id, targetEntry.getCard().getName(), targetManaValue, entry.getXValue());
        }
    }
}
