package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "91")
public class EmberGale extends Card {

    private static final PermanentPredicate WHITE_OR_BLUE_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentColorInPredicate(Set.of(CardColor.WHITE, CardColor.BLUE))
    ));

    public EmberGale() {
        // Creatures target player controls can't block this turn.
        addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS));

        // Ember Gale deals 1 damage to each white and/or blue creature that player controls.
        // Rides the shared player target from the can't-block effect above.
        addEffect(EffectSlot.SPELL, new DealDamageToEachMatchingPermanentEffect(
                1, WHITE_OR_BLUE_CREATURE, EachPermanentScope.TARGET_PLAYER));
    }
}
