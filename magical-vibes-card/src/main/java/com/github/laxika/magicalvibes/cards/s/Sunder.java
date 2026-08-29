package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "USG", collectorNumber = "101")
public class Sunder extends Card {

    public Sunder() {
        addEffect(EffectSlot.SPELL,
                ReturnToHandEffect.allPermanentsMatching(new PermanentIsLandPredicate()));
    }
}
