package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

@CardRegistration(set = "USG", collectorNumber = "176")
public class Brand extends Card {

    public Brand() {
        addEffect(EffectSlot.SPELL,
                new GainControlOfAllPermanentsMatchingEffect(new PermanentOwnedBySourceControllerPredicate()));
        addCycling("{2}");
    }
}
