package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MAT", collectorNumber = "48")
@CardRegistration(set = "MAT", collectorNumber = "98")
@CardRegistration(set = "MAT", collectorNumber = "148")
@CardRegistration(set = "MAT", collectorNumber = "183")
@CardRegistration(set = "MAT", collectorNumber = "227")
public class TyvarTheBellicose extends Card {

    public TyvarTheBellicose() {
        addTrigger(Triggers.youAttack().withFilter(new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.triggeringAttackers()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.thisManaAbilityResolves(),
                        TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),
                Subjects.creaturesYouControl()));
    }
}
