package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "143")
public class DwarvenSong extends Card {

    public DwarvenSong() {
        target(TargetFilters.creature(), 1, 99)
                .addEffect(EffectSlot.SPELL,
                        new GrantColorUntilEndOfTurnEffect(CardColor.RED, GrantScope.TARGETS));
    }
}
