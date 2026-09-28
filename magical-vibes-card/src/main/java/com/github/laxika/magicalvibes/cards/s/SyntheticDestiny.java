package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesYouControlAndRegisterDelayedRevealEffect;

@CardRegistration(set = "C15", collectorNumber = "15")
public class SyntheticDestiny extends Card {

    public SyntheticDestiny() {
        addEffect(EffectSlot.SPELL, new ExileAllCreaturesYouControlAndRegisterDelayedRevealEffect());
    }
}
