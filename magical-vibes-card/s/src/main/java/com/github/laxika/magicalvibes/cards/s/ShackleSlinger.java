package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "65")
public class ShackleSlinger extends Card {

    public ShackleSlinger() {
        var tappedTarget = new TargetPermanentMatches(new PermanentIsTappedPredicate());
        var untappedTarget = new TargetPermanentMatches(
                new PermanentNotPredicate(new PermanentIsTappedPredicate()));

        // Whenever you cast your second spell each turn, choose target creature an opponent
        // controls. If it's tapped, put a stun counter on it. Otherwise, tap it.
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new ConditionalEffect(tappedTarget, new PutCounterOnTargetPermanentEffect(CounterType.STUN)),
                new ConditionalEffect(untappedTarget, new TapPermanentsEffect(TapUntapScope.TARGET)));
    }
}
