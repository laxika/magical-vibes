package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMulticoloredEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "9")
@CardRegistration(set = "M21", collectorNumber = "288")
public class BasrisLieutenant extends Card {

    public BasrisLieutenant() {
        addStaticEffect(new ProtectionFromMulticoloredEffect());
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        // "If it had a +1/+1 counter on it" is about whichever creature died, this one included: one ability with two
        // trigger conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE), ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                                ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new CreateTokenEffect(1, "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of())))));
    }
}
