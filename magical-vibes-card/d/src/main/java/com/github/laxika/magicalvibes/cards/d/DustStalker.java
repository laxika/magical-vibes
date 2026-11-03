package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "202")
public class DustStalker extends Card {

    public DustStalker() {
        // At the beginning of each end step, if you control no other colorless creatures,
        // return this creature to its owner's hand.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NoOtherPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsColorlessPredicate(),
                                new PermanentIsCreaturePredicate())))),
                ReturnToHandEffect.self());
    }
}
