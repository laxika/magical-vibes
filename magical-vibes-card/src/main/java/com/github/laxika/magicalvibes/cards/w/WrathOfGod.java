package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "54")
@CardRegistration(set = "7ED", collectorNumber = "57")
@CardRegistration(set = "10E", collectorNumber = "61")
@CardRegistration(set = "8ED", collectorNumber = "58")
@CardRegistration(set = "9ED", collectorNumber = "56")
@CardRegistration(set = "POR", collectorNumber = "39")
@CardRegistration(set = "5ED", collectorNumber = "69")
public class WrathOfGod extends Card {

    public WrathOfGod() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true));
    }
}
