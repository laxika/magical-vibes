package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
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
                new CounterUnlessPaysEffect(2)), GrantScope.ALL_PERMANENTS, commander));

        // Whenever your commander attacks, put two +1/+1 counters on it.
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.attacks(),
                new PutCountersOnSourceEffect(1, 1, 2)), GrantScope.ALL_CREATURES, commander));

        // When your commander leaves the battlefield, abandon this scheme.
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER),
                new TriggeringPermanentConditionalEffect(commander, new SacrificeSelfEffect()));
    }
}
