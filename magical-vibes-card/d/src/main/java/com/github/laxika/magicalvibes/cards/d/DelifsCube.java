package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "85")
@CardRegistration(set = "FEM", collectorNumber = "170")
public class DelifsCube extends Card {

    public DelifsCube() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect(new TriggeredAbility(
                        Triggers.attacksUnblocked(),
                        AssignNoCombatDamageEffect.ofTriggeringPermanent(),
                        new PutCountersOnSourceCardEffect(CounterType.CUBE)))),
                "{2}, {T}: This turn, when target creature you control attacks and isn't blocked, it "
                        + "assigns no combat damage this turn and you put a cube counter on this artifact.",
                TargetFilters.creatureYouControl()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RemoveCounterFromSourceCost(1, CounterType.CUBE), new RegenerateEffect(true)),
                "{2}, Remove a cube counter from this artifact: Regenerate target creature.",
                TargetFilters.creature()
        ));
    }
}
