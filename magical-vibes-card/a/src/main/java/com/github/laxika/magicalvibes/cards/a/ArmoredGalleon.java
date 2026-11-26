package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "33")
public class ArmoredGalleon extends Card {

    public ArmoredGalleon() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));
    }
}
