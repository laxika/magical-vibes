package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;

@CardRegistration(set = "PTK", collectorNumber = "128")
public class YuanShaoTheIndecisive extends Card {

    public YuanShaoTheIndecisive() {
        // Horsemanship is auto-loaded from Scryfall.
        // "Each creature you control can't be blocked by more than one creature."
        addStaticEffect(new EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
