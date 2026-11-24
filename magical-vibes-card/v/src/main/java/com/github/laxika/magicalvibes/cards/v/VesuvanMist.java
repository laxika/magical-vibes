package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaToCastEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "7")
public class VesuvanMist extends Card {

    public VesuvanMist() {
        addStaticEffect(new KickerEffect("{1}{B}"));

        PermanentAllOfPredicate nontokenNonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        target(new PermanentPredicateTargetFilter(
                nontokenNonland,
                "Target must be a nontoken, nonland permanent"
        )).addSpellEffect(ReturnToHandEffect.target());
        // "That card" left the battlefield for a hand, so its duplicate reads it as it last existed there (CR 608.2h).
        addSpellEffect(new ConditionalEffect(new Kicked(), SequenceEffect.of(
                ConjureCardsEffect.duplicateOf(Subjects.departedThisWay(StepKind.RETURN), ConjureDestination.intoHand()),
                new PerpetuallyGrantAnyColorManaToCastEffect(Subjects.cardsThisWay(StepKind.CONJURE)))));
    }
}
