package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RecordTargetSpellManaValueAsXEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecordTargetSpellManaValueAsXEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RecordTargetSpellManaValueAsXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry targetEntry = counterSupport.findCounterTargetIgnoringCounterability(
                gameData, entry.getTargetId(), entry);
        if (targetEntry == null) {
            return;
        }
        var targetCard = targetEntry.getTargetingCard();
        int xSymbols = targetCard.getManaCost() == null ? 0
                : new ManaCost(targetCard.getManaCost()).getXSymbolCount();
        entry.setXValue(targetCard.getManaValue() + xSymbols * targetEntry.getXValue());
    }
}
