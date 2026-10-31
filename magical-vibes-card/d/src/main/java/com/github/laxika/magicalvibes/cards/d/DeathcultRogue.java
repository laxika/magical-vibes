package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "GTC", collectorNumber = "218")
public class DeathcultRogue extends Card {

    public DeathcultRogue() {
        addStaticEffect(new CanBeBlockedOnlyByFilterEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ROGUE),
                "Rogues"));
    }
}
