package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "341")
public class MyChampionStandsSupreme extends Card {

    public MyChampionStandsSupreme() {
        PermanentIsCommanderPredicate commander = new PermanentIsCommanderPredicate(true);

        // Your commander has ward {2}.
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(2)), Subjects.otherPermanents(), commander));

        // Whenever your commander attacks, put two +1/+1 counters on it.
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.attacks(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2)), Subjects.otherCreatures(), commander));

        // When your commander leaves the battlefield, abandon this scheme.
        addTrigger(Triggers.leavesBattlefield(commander, ObjectRelation.ANOTHER), new SacrificeSelfEffect());
    }
}
