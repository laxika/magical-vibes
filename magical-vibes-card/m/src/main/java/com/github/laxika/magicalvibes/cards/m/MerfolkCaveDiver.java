package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "65")
public class MerfolkCaveDiver extends Card {

    public MerfolkCaveDiver() {
        // Whenever a creature you control explores, this creature gets +1/+0 until end of turn
        // and can't be blocked this turn.
        addTrigger(Triggers.creatureYouControlExplores(), SequenceEffect.of(
                new BoostSelfEffect(1, 0),
                new MakeCreatureUnblockableEffect(true)));
    }
}
