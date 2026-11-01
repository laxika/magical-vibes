package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "115")
@CardRegistration(set = "DBL", collectorNumber = "115")
public class Necrosynthesis extends Card {

    public Necrosynthesis() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANOTHER),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new LookAtTopCardsEffect(
                                new EventValue(), new Fixed(1), null,
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                                LibrarySearchDestination.HAND, false));
    }
}
