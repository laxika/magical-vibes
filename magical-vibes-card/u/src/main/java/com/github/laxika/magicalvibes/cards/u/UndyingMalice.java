package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "VOW", collectorNumber = "134")
@CardRegistration(set = "FDN", collectorNumber = "528")
@CardRegistration(set = "DBL", collectorNumber = "401")
public class UndyingMalice extends Card {

    public UndyingMalice() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                        new PutCardsOntoBattlefieldEffect(Subjects.sourceObjectCard()).enteringTapped()
                                .withCounters(CounterType.PLUS_ONE_PLUS_ONE, 1))));
    }
}
