package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "65")
public class ZhouYuChiefCommander extends Card {

    public ZhouYuChiefCommander() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));
    }
}
