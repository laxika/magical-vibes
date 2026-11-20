package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "140")
public class KunLunWarrior extends Card {

    public KunLunWarrior() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Sacrifice an artifact",
                                        new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(), Subjects.you())),
                                new ChooseOneEffect.ChooseOneOption("Discard a card",
                                        new DiscardEffect(1, Subjects.you()))))),
                        new ConditionalEffect(
                                new AnyOf(List.of(HappenedThisWay.sacrificed(), new HappenedThisWay(StepKind.DISCARD))),
                                new DrawCardEffect(1))),
                "Sacrifice an artifact or discard a card?"));
    }
}
