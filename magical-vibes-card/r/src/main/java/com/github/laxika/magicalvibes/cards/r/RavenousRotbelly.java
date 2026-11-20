package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "849")
@CardRegistration(set = "MIC", collectorNumber = "22")
@CardRegistration(set = "MIC", collectorNumber = "60")
public class RavenousRotbelly extends Card {

    public RavenousRotbelly() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        SacrificePermanentsEffect.upTo(
                                3, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), Subjects.you()),
                        new ConditionalEffect(
                                HappenedThisWay.sacrificed(),
                                new QueueReflexiveAbilityEffect(
                                        new SacrificePermanentsEffect(
                                                new AmountThisWay(StepKind.SACRIFICE),
                                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                                                Subjects.eachOpponent())))),
                "Sacrifice up to three Zombies?"));
    }
}
