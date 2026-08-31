package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBottomThenDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;

@CardRegistration(set = "ONS", collectorNumber = "214")
public class Kaboom extends Card {

    public Kaboom() {
        target(0, 99).addEffect(EffectSlot.SPELL,
                new RevealUntilNonlandBottomThenDealManaValueDamageEffect(
                        TargetPredicates.playerOrPlaneswalker()));
    }
}
