package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "VMA", collectorNumber = "248")
public class DacksDuplicate extends Card {

    public DacksDuplicate() {
        // You may have this creature enter as a copy of any creature on the battlefield, except it
        // has haste and dethrone.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", Set.of(),
                Set.of(Keyword.HASTE, Keyword.DETHRONE), null, false
        ));
    }
}
