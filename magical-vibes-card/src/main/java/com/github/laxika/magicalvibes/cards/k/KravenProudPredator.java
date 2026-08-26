package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SPM", collectorNumber = "132")
public class KravenProudPredator extends Card {

    public KravenProudPredator() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate()),
                new Fixed(4)));
    }
}
