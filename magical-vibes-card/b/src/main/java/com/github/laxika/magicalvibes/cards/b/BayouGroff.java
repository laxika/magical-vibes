package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "STX", collectorNumber = "121")
public class BayouGroff extends Card {

    public BayouGroff() {
        // As an additional cost to cast this spell, sacrifice a creature or pay {3}.
        addCastingCost(AnyOfCost.of(new SacrificeCreatureCost(), new PayManaCost("{3}")));
    }
}
