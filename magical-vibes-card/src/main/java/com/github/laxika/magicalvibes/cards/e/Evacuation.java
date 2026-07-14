package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "83")
@CardRegistration(set = "9ED", collectorNumber = "75")
@CardRegistration(set = "8ED", collectorNumber = "76")
@CardRegistration(set = "7ED", collectorNumber = "72")
public class Evacuation extends Card {

    public Evacuation() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(new PermanentIsCreaturePredicate()));
    }
}
