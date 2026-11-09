package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "54")
public class ZombieBoa extends Card {

    public ZombieBoa() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new ChooseColorAtResolutionEffect(),
                        new GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER,
                                new DestroyCreatureBlockingThisEffect()))
                ),
                "{1}{B}: Choose a color. Whenever Zombie Boa becomes blocked by a creature of that color this turn, destroy that creature.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
