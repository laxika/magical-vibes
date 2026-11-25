package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "657")
@CardRegistration(set = "MH1", collectorNumber = "183")
public class TemperedSliver extends Card {

    public TemperedSliver() {
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                Subjects.creaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
