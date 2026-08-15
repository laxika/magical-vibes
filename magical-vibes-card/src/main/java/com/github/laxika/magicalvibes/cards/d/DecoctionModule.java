package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "205")
public class DecoctionModule extends Card {

    public DecoctionModule() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new EnergyCountersEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(ReturnToHandEffect.target()),
                "{4}, {T}: Return target creature you control to its owner's hand.",
                TargetFilters.creatureYouControl()
        ));
    }
}
