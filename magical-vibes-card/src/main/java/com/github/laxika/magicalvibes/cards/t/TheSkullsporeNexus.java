package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithTotalDyingCreaturesPowerEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "212")
public class TheSkullsporeNexus extends Card {

    public TheSkullsporeNexus() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new GreatestPowerAmongControlled()));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new CreateTokenWithTotalDyingCreaturesPowerEffect(
                        new CreateTokenEffect("Fungus Dinosaur", 0, 0, CardColor.GREEN,
                                List.of(CardSubtype.FUNGUS, CardSubtype.DINOSAUR), Set.of(), Set.of())));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DoubleTargetCreaturePowerEffect(new Fixed(1))),
                "{2}, {T}: Double target creature's power until end of turn."
        ));
    }
}
