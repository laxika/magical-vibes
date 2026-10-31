package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "CHK", collectorNumber = "178")
@CardRegistration(set = "MMA", collectorNumber = "121")
@CardRegistration(set = "SLD", collectorNumber = "997")
@CardRegistration(set = "SLD", collectorNumber = "2049")
@CardRegistration(set = "SLD", collectorNumber = "2054")
@CardRegistration(set = "UMA", collectorNumber = "136")
@CardRegistration(set = "TSR", collectorNumber = "347")
public class LavaSpike extends Card {

    public LavaSpike() {
        // Lava Spike deals 3 damage to target player or planeswalker.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"
        )).addSpellEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(3));
    }
}
