package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.HighestLifeTotalAmongPlayers;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "ZNR", collectorNumber = "122")
public class ScourgeOfTheSkyclaves extends Card {

    public ScourgeOfTheSkyclaves() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}{B}"));
        addEffect(EffectSlot.ON_SELF_CAST, new ConditionalEffect(new Kicked(),
                new EachPlayerLosesFractionOfLifeRoundedUpEffect(2)));

        var powerAndToughness = new Sum(
                new Fixed(20),
                new Scaled(new HighestLifeTotalAmongPlayers(), -1));
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(powerAndToughness, powerAndToughness));
    }
}
