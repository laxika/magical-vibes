package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "97")
public class Somnophore extends Card {

    public Somnophore() {
        addTrigger(Triggers.dealsDamageToPlayer(),
                TapChosenPermanentEffect.damagedPlayerControls(new PermanentIsCreaturePredicate()));
    }
}
