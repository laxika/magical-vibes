package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "38")
public class WarFalcon extends Card {

    public WarFalcon() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new ControlsPermanentCount(1, new PermanentHasAnySubtypePredicate(
                        Set.of(CardSubtype.KNIGHT, CardSubtype.SOLDIER))),
                "you control a Knight or a Soldier"
        ));
    }
}
