package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "CON", collectorNumber = "101")
public class ChildOfAlara extends Card {

    public ChildOfAlara() {
        // When Child of Alara dies, destroy all nonland permanents. They can't be regenerated.
        addEffect(EffectSlot.ON_DEATH,
                new DestroyAllPermanentsEffect(new PermanentNotPredicate(new PermanentIsLandPredicate()), true));
    }
}
