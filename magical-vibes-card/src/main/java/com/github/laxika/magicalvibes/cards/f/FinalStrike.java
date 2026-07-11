package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "POR", collectorNumber = "94")
public class FinalStrike extends Card {

    public FinalStrike() {
        // As an additional cost, sacrifice a creature; snapshots its power into the entry's xValue.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true));

        // Deals damage equal to the sacrificed creature's power to target opponent or planeswalker.
        // AnyTargetPredicateTargetFilter enforces opponent-only players at the card level (using the
        // caster's controller), which the effect-level validator can't for a spell with no permanent source.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker"
        )).addEffect(EffectSlot.SPELL, new DealDamageToTargetOpponentOrPlaneswalkerEffect(new XValue()));
    }
}
