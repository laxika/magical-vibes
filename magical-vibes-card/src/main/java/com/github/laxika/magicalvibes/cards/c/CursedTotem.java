package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "6ED", collectorNumber = "278")
public class CursedTotem extends Card {

    public CursedTotem() {
        addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(
                new PermanentIsCreaturePredicate()
        ));
    }
}
