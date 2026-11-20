package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "222")
public class ArgentumMasticore extends Card {

    public ArgentumMasticore() {
        var opponentNonlandPermanentAtMostDiscardedManaValue = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentManaValueAtMostXPredicate()));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                DiscardCardThenEffect.withDiscardedManaValueAsX(
                        new DestroyTargetPermanentEffect(opponentNonlandPermanentAtMostDiscardedManaValue)),
                "Discard a card? If you don't, sacrifice this creature.",
                new SacrificeSelfEffect()));
    }
}
