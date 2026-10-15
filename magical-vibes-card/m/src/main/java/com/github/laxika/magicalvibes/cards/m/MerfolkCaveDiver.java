package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "LCI", collectorNumber = "65")
public class MerfolkCaveDiver extends Card {

    public MerfolkCaveDiver() {
        // Whenever a creature you control explores, this creature gets +1/+0 until end of turn
        // and can't be blocked this turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_EXPLORES, SequenceEffect.of(
                new BoostSelfEffect(1, 0),
                new MakeCreatureUnblockableEffect(true)));
    }
}
