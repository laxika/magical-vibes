package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.List;

/**
 * Total War — "Whenever a player attacks with one or more creatures, destroy all untapped non-Wall
 * creatures that player controls that didn't attack, except for creatures the player hasn't
 * controlled continuously since the beginning of the turn."
 *
 * <p>The trigger fires once per combat for any attacking player ({@link
 * EffectSlot#ON_ANY_PLAYER_ATTACKS}, which stores that player as the non-targeting target), so the
 * sweep is scoped to {@link EachPermanentScope#TARGET_PLAYER}. "Hasn't controlled continuously
 * since the beginning of the turn" is the summoning-sickness signal, so creatures that just arrived
 * are spared even if they have haste and stayed home.
 */
@CardRegistration(set = "ICE", collectorNumber = "221")
public class TotalWar extends Card {

    public TotalWar() {
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTappedPredicate()),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL)),
                        new PermanentNotPredicate(new PermanentAttackedThisTurnPredicate()),
                        new PermanentControlledContinuouslySinceBeginningOfTurnPredicate())),
                EachPermanentScope.TARGET_PLAYER,
                null));
    }
}
