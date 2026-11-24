package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleEnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "M3C", collectorNumber = "54")
@CardRegistration(set = "M3C", collectorNumber = "106")
public class AetherRefinery extends Card {

    public AetherRefinery() {
        addStaticEffect(new DoubleEnergyCountersEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new EnergyCountersEffect(1),
                        EachPlayerPaysAnyManaEffect.byYou(Resource.ENERGY),
                        new ConditionalEffect(new HappenedThisWay(StepKind.PAY_ENERGY),
                                new CreateTokenEffect("Aetherborn", AmountThisWay.energyPaid(),
                                        AmountThisWay.energyPaid(), CardColor.BLACK,
                                        List.of(CardSubtype.AETHERBORN), Set.of(), Set.of()))),
                "{T}: You get {E}, then you may pay one or more {E}. If you do, create an X/X black "
                        + "Aetherborn creature token, where X is the amount of {E} paid this way."
        ));
    }
}
