package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "103")
public class MindlessNull extends Card {

    public MindlessNull() {
        // This creature can't block unless you control a Vampire.
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new ControlsPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)),
                "you control a Vampire"
        ));
    }
}
