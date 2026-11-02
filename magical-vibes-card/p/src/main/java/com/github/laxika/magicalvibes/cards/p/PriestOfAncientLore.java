package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "35")
@CardRegistration(set = "HBG", collectorNumber = "99")
public class PriestOfAncientLore extends Card {

    public PriestOfAncientLore() {
        // When this creature enters, you gain 1 life and draw a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new GainLifeEffect(1),
                new DrawCardEffect(1)));
    }
}
