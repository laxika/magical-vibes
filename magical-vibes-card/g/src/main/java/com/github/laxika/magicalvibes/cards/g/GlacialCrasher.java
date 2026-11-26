package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M15", collectorNumber = "57")
public class GlacialCrasher extends Card {

    public GlacialCrasher() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new AnyPlayerControlsPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                "a Mountain on the battlefield"
        ));
    }
}
