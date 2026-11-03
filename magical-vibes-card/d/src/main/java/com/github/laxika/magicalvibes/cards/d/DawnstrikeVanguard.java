package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "10")
public class DawnstrikeVanguard extends Card {

    private static final PermanentPredicate TAPPED_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsTappedPredicate()));
    private static final PermanentPredicate OTHER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

    public DawnstrikeVanguard() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanentCount(2, TAPPED_CREATURE)),
                new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, OTHER_CREATURE));
    }
}
