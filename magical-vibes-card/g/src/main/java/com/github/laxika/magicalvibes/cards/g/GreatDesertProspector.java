package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BRO", collectorNumber = "7")
public class GreatDesertProspector extends Card {

    public GreatDesertProspector() {
        // When this creature enters, create a tapped Powerstone token for each other creature you control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofPowerstoneToken(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true)));
    }
}
