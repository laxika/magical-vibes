package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "204")
@CardRegistration(set = "WOE", collectorNumber = "362")
@CardRegistration(set = "SOC", collectorNumber = "312")
public class TheGooseMother extends Card {

    public TheGooseMother() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addTrigger(Triggers.enters(ObjectRelation.THIS), foodTokens());
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.FOOD), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new DrawCardEffect())),
                "Sacrifice a Food?"));
    }

    private static CreateTokenEffect foodTokens() {
        return CreateTokenEffect.ofArtifactToken(
                new HalvedRoundedUp(new XValue()),
                "Food",
                List.of(CardSubtype.FOOD),
                List.of(new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }
}
