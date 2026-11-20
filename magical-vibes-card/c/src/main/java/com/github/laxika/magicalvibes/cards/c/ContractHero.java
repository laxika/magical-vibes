package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "683")
public class ContractHero extends Card {

    public ContractHero() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Sacrifice an artifact",
                                        new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(), Subjects.you())),
                                new ChooseOneEffect.ChooseOneOption("Discard a card",
                                        new DiscardEffect(1, Subjects.you()))))),
                        new ConditionalEffect(
                                new AnyOf(List.of(HappenedThisWay.sacrificed(), new HappenedThisWay(StepKind.DISCARD))),
                                new BoostSelfEffect(2, 0))),
                "Sacrifice an artifact or discard a card?"));
    }
}
