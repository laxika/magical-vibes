package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "35")
public class SealFromExistence extends Card {

    public SealFromExistence() {
        // Ward {3}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(3));

        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
