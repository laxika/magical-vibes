package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "151")
public class ChiefWargsCompany extends Card {

    public ChiefWargsCompany() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControlsOtherPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.WOLF)),
                "you control two or more other Wolves"
        ));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                "Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of()));
    }
}
