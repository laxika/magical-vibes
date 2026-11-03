package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.LandPlayFromExileTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "28")
public class ScalesoulGnome extends Card {

    public ScalesoulGnome() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DiscoverEffect(new EventValue()));

        List<CardEffect> duplicate =
                List.of(new ConjureDuplicateOfTriggeringCardIntoHandEffect());
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.EXILE)),
                duplicate.toArray(CardEffect[]::new));
        addTrigger(Triggers.youPlayLand(),
                new LandPlayFromExileTriggerEffect(duplicate));
    }
}
