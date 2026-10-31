package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SPM", collectorNumber = "132")
@CardRegistration(set = "OM1", collectorNumber = "135")
public class KravenProudPredator extends Card {

    public KravenProudPredator() {
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate()),
                new Fixed(4)));
    }
}
