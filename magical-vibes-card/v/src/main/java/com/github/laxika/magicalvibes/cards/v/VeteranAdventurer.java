package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypesToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "218")
public class VeteranAdventurer extends Card {

    public VeteranAdventurer() {
        addStaticEffect(new GrantSubtypesToSelfEffect(List.of(
                CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD)));
        addStaticEffect(new ReduceOwnCastCostEffect(new PartySize()));
    }
}
