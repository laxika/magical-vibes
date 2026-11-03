package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "114")
public class PhoenixFleetAirship extends Card {

    public PhoenixFleetAirship() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerSacrificedPermanentThisTurn()),
                new CreateTokenCopyOfSourceEffect());

        addStaticEffect(new ConditionalEffect(
                new ControlsPermanentCount(8, new PermanentNamedPredicate("Phoenix Fleet Airship")),
                AnimatePermanentsEffect.crew()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"));
    }
}
