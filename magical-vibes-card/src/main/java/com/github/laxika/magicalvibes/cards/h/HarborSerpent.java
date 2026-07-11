package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "56")
public class HarborSerpent extends Card {

    public HarborSerpent() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new AnyPlayerControlsPermanentCount(5, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "five or more Islands on the battlefield"
        ));
    }
}
