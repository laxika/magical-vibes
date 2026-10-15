package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "30")
public class SentinelOfTheEternalWatch extends Card {

    public SentinelOfTheEternalWatch() {
        // At the beginning of combat on each opponent's turn, tap target creature that player controls.
        // "That player" is the opponent whose turn it is — i.e. the active player, since the trigger
        // only fires on opponents' turns.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByActivePlayerPredicate()
                )),
                "Target must be a creature controlled by the player whose turn it is"
        )).addEffect(EffectSlot.OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED,
                new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
