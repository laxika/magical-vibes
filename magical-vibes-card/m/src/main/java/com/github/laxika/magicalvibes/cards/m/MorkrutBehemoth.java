package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "MID", collectorNumber = "114")
@CardRegistration(set = "DBL", collectorNumber = "114")
public class MorkrutBehemoth extends Card {

    public MorkrutBehemoth() {
        // As an additional cost to cast this spell, sacrifice a creature or pay {1}{B}.
        addCastingCost(AnyOfCost.of(new SacrificeCreatureCost(), new PayManaCost("{1}{B}")));
    }
}
