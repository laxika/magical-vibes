package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "229")
public class ShowdownOfTheSkalds extends Card {

    public ShowdownOfTheSkalds() {
        addTrigger(Triggers.chapter(1), new ExileTopCardMayPlayThisTurnEffect(4, false)
                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));

        var targetCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        var targetFilter = TargetFilters.creatureYouControl();
        var counterEffect = PutCounterOnTargetPermanentEffect.withTargetRestriction(
                CounterType.PLUS_ONE_PLUS_ONE, 1, targetCreatureYouControl);
        addTrigger(Triggers.chapter(2),
                RegisterDelayedControllerSpellCastTriggerEffect.thisTurn(null, List.of(counterEffect))
                .withTargetFilter(targetFilter));
        addTrigger(Triggers.chapter(3),
                RegisterDelayedControllerSpellCastTriggerEffect.thisTurn(null, List.of(counterEffect))
                .withTargetFilter(targetFilter));
    }
}
