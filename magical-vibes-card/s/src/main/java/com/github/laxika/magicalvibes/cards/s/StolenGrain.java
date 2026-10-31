package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PTK", collectorNumber = "83")
@CardRegistration(set = "ME3", collectorNumber = "75")
public class StolenGrain extends Card {

    public StolenGrain() {
        // Deals 5 damage to target opponent or planeswalker and you gain 5 life.
        // AnyTargetPredicateTargetFilter enforces opponent-only players at the card level.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker"
        )).addSpellEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(5, PlayerRelation.OPPONENT));

        addSpellEffect(new GainLifeEffect(5));
    }
}
