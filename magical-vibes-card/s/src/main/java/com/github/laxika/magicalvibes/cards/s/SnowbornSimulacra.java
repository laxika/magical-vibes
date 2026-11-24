package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaToCastEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "HBG", collectorNumber = "38")
public class SnowbornSimulacra extends Card {

    public SnowbornSimulacra() {
        PermanentNotPredicate nontoken = new PermanentNotPredicate(new PermanentIsTokenPredicate());
        targetExactlyX(new PermanentPredicateTargetFilter(nontoken, "Targets must be nontoken permanents"), 100)
                .addSpellEffect(ConjureCardsEffect.duplicateOfTarget(nontoken, ConjureDestination.intoHand()));
        addSpellEffect(new PerpetuallyGrantAnyColorManaToCastEffect(Subjects.cardsThisWay(StepKind.CONJURE)));
        addSpellEffect(new ConditionalEffect(new SpellXAtLeast(5), SequenceEffect.of(
                ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.CONJURE), 1,
                        "one of the conjured cards to put onto the battlefield"),
                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)))));
    }
}
