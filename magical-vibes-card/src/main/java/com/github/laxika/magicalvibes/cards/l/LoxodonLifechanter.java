package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.TotalToughnessOfControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "27")
public class LoxodonLifechanter extends Card {

    public LoxodonLifechanter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SetLifeTotalEffect(new TotalToughnessOfControlledCreatures()),
                "Have your life total become the total toughness of creatures you control?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}",
                List.of(new BoostSelfEffect(new ControllerLifeTotal(), new ControllerLifeTotal())),
                "This creature gets +X/+X until end of turn, where X is your life total."
        ));
    }
}
