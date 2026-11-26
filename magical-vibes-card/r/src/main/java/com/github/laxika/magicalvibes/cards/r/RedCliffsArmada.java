package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "51")
@CardRegistration(set = "ME2", collectorNumber = "62")
public class RedCliffsArmada extends Card {

    public RedCliffsArmada() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));
    }
}
