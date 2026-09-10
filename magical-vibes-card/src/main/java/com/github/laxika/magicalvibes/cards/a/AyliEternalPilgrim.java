package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "151")
public class AyliEternalPilgrim extends Card {

    public AyliEternalPilgrim() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, true, true),
                        new GainLifeEffect(new XValue())
                ),
                "{1}, Sacrifice another creature: You gain life equal to the sacrificed creature's toughness."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new ExileTargetPermanentEffect()
                ),
                "{1}{W}{B}, Sacrifice another creature: Exile target nonland permanent. Activate only if you have at least 10 life more than your starting life total.",
                TargetFilters.nonlandPermanent()
        ).withActivationCondition(
                new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL + 10),
                "Activate only if you have at least 10 life more than your starting life total."
        ));
    }
}
