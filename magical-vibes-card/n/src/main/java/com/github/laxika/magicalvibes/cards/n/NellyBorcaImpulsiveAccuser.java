package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSuspectedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "4")
@CardRegistration(set = "MKC", collectorNumber = "52")
@CardRegistration(set = "MKC", collectorNumber = "318")
public class NellyBorcaImpulsiveAccuser extends Card {

    public NellyBorcaImpulsiveAccuser() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(), new SuspectEffect(GrantScope.TARGET));
        addTrigger(Triggers.attacks(),
                new GoadCreaturesUntilNextTurnSnapshotEffect(new PermanentIsSuspectedPredicate()));

        addTrigger(Triggers.creatureDealsCombatDamageToOpponent()
                        .withFilter(new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())),
                SequenceEffect.of(new DrawCardEffect(1), new DrawCardForTriggeringPermanentControllerEffect()));
    }
}
