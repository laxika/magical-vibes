package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantJumpStartToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "56")
@CardRegistration(set = "M3C", collectorNumber = "108")
public class FiligreeRacer extends Card {

    public FiligreeRacer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(4));
        addEffect(EffectSlot.ON_ATTACK, new ForcedCostOrElseEffect(
                new PayEnergyCost(2),
                List.of(),
                true,
                List.of(new QueueReflexiveAbilityEffect(new GrantJumpStartToTargetGraveyardCardEffect()))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
