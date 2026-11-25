package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;

@CardRegistration(set = "WHO", collectorNumber = "21")
@CardRegistration(set = "WHO", collectorNumber = "626")
public class TheGirlInTheFireplace extends Card {

    public TheGirlInTheFireplace() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                1, "Human Noble", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.NOBLE), Set.of(Keyword.VANISHING), Set.of(),
                List.of(
                        StaticAbility.of(new EnterWithCountersEffect(CounterType.TIME, new Fixed(3))),
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                                RemoveCountersEffect.fromSelf(CounterType.TIME, 1)),
                        StaticAbility.of(new PreventAllDamageEffect()))));

        addTrigger(Triggers.chapter(2), new CreateTokenEffect(
                1, "Horse", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.HORSE), Set.of(), Set.of(),
                List.of(
                        StaticAbility.of(new GrantKeywordEffect(
                        Keyword.HORSEMANSHIP, Subjects.creaturesYouControl(),
                        new PermanentHasSubtypePredicate(CardSubtype.DOCTOR))))));

        addTrigger(Triggers.chapter(3), new RegisterGlobalTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureYouControlDealsCombatDamageToPlayer(), new TimeTravelEffect(1))));
    }
}
