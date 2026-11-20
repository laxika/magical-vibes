package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "146")
@CardRegistration(set = "HOC", collectorNumber = "146")
public class BardsCompany extends Card {

    public BardsCompany() {
        setFlashCastCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl()));

        CardEffect recruit = SequenceEffect.of(
                new DrawCardEffect(1),
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.DISCARD, new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                                new CreateTokenEffect("Human Soldier", 1, 1, CardColor.WHITE,
                                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), recruit);
        addTrigger(Triggers.attacks(), recruit);
    }
}
