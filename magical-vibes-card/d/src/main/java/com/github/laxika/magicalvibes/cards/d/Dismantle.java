package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CountersOnTargetPermanent;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "57")
@CardRegistration(set = "2XM", collectorNumber = "123")
public class Dismantle extends Card {

    public Dismantle() {
        target(TargetFilters.artifact())
                .addSpellEffect(new NoteAmountEffect(new CountersOnTargetPermanent(null)))
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(new HappenedThisWay(StepKind.NOTE),
                        new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("+1/+1 counters",
                                        countersOnAnArtifactYouControl(CounterType.PLUS_ONE_PLUS_ONE)),
                                new ChooseOneEffect.ChooseOneOption("charge counters",
                                        countersOnAnArtifactYouControl(CounterType.CHARGE)))))));
    }

    private static List<CardEffect> countersOnAnArtifactYouControl(CounterType counterType) {
        return List.of(
                ChoosePermanentsEffect.exactly(1,
                        Subjects.controlledBy(Subjects.you(), new PermanentIsArtifactPredicate()),
                        "an artifact you control"),
                new PutCounterOnTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE), counterType,
                        AmountThisWay.noted()));
    }
}
