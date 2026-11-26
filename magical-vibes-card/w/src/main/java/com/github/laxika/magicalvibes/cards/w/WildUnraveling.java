package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;

@CardRegistration(set = "ECL", collectorNumber = "84")
public class WildUnraveling extends Card {

    public WildUnraveling() {
        addCastingCost(AnyOfCost.of(PutCounterOnControlledCreatureCost.blight(2), new PayManaCost("{1}")));
        addSpellEffect(new CounterSpellEffect());
    }
}
