package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "36")
@CardRegistration(set = "MOC", collectorNumber = "123")
public class ConclaveSledgeCaptain extends Card {

    public ConclaveSledgeCaptain() {
        setAllowSharedTargets(true);

        CardEffect combatDamageTrigger = new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new EventValue());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, combatDamageTrigger);

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        // Backup 1, backup 1, backup 1 are three separate target choices and triggers.
        for (int i = 0; i < 3; i++) {
            target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                    SequenceEffect.of(
                            new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                            new ConditionalEffect(
                                    new TargetPermanentMatches(anotherCreature),
                                    SequenceEffect.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET),
                                            new GrantEffectToTargetUntilEndOfTurnEffect(
                                            EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                            combatDamageTrigger))
                            )
                    ));
        }
    }
}
