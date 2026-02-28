package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "22")
public class SunblastAngel extends Card {

    public SunblastAngel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyAllPermanentsEffect(
                Set.of(CardType.CREATURE), false, false, new PermanentIsTappedPredicate()));
    }
}
