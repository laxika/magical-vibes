package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CastAnyNumberOfSpellsFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class TheGreatSynthesis extends Card {

    public TheGreatSynthesis() {
        addTrigger(Triggers.chapter(1), SequenceEffect.of(
                new DrawCardEffect(new CardsInHand(CountScope.CONTROLLER)),
                new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.WHILE_SOURCE_ON_BATTLEFIELD)));
        addTrigger(Triggers.chapter(2), ReturnToHandEffect.allPermanentsMatching(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.PHYREXIAN))
                ))));
        addTrigger(Triggers.chapter(3), SequenceEffect.of(
                new CastAnyNumberOfSpellsFromHandWithoutPayingManaCostEffect(),
                new FlickerEffect(FlickerScope.SELF, null, ReturnTiming.IMMEDIATE, TurnStep.END_STEP,
                        false, null, null, 0, false, false)));
    }
}
