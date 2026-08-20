package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnStackEntryCardEffect;
import org.springframework.stereotype.Component;

/** Resolves counters placed on the source spell's stack object. */
@Component
public class PutCountersOnStackEntryCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnStackEntryCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourceStackCardId() == null) {
            return;
        }
        StackEntry sourceEntry = gameData.stack.stream()
                .filter(candidate -> candidate.getEntryType() != StackEntryType.ACTIVATED_ABILITY)
                .filter(candidate -> candidate.getCard() != null)
                .filter(candidate -> candidate.getCard().getId().equals(entry.getSourceStackCardId()))
                .findFirst()
                .orElse(null);
        if (sourceEntry == null) {
            return;
        }

        PutCountersOnStackEntryCardEffect counters = (PutCountersOnStackEntryCardEffect) effect;
        sourceEntry.setCounterCount(counters.counterType(),
                sourceEntry.getCounterCount(counters.counterType()) + counters.count());
    }
}
