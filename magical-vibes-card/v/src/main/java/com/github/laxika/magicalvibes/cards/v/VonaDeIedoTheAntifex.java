package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetualAnyManaTypeToCastSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "28")
public class VonaDeIedoTheAntifex extends Card {

    public VonaDeIedoTheAntifex() {
        PermanentAllOfPredicate nonlandNontokenOpponents = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        target(new PermanentPredicateTargetFilter(nonlandNontokenOpponents,
                "Target must be a nonland, nontoken permanent an opponent controls"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new DestroyTargetPermanentEffect(nonlandNontokenOpponents),
                        new MayEffect(SequenceEffect.of(
                                new DiscardEffect(1, Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.discarded(),
                                        ConjureCardsEffect.duplicateOf(Subjects.targetDeclaredElsewhere(),
                                                        ConjureDestination.intoHand())
                                                .withAbilities(List.of(StaticAbility.of(List.of(
                                                        new PerpetualAnyManaTypeToCastSelfEffect())))))),
                                "Discard a card to conjure a duplicate of that permanent?"));
    }
}
