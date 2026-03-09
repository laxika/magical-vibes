package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "56")
public class HarborSerpent extends Card {

    public HarborSerpent() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
                5,
                "five or more Islands on the battlefield"
        ));
    }
}
