package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsCantBeTargetedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M13", collectorNumber = "176")
@CardRegistration(set = "ODY", collectorNumber = "242")
@CardRegistration(set = "WOT", collectorNumber = "54")
@CardRegistration(set = "C18", collectorNumber = "149")
public class GroundSeal extends Card {

    public GroundSeal() {
        // When this enchantment enters, draw a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));

        // Cards in graveyards can't be the targets of spells or abilities.
        addStaticEffect(new GraveyardCardsCantBeTargetedEffect());
    }
}
