package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "9ED", collectorNumber = "182")
@CardRegistration(set = "STH", collectorNumber = "81")
@CardRegistration(set = "TPR", collectorNumber = "131")
public class FlameWave extends Card {

    public FlameWave() {
        // Flame Wave deals 4 damage to target player or planeswalker and each creature
        // that player or that planeswalker's controller controls.
        DealDamageToTargetPlayerOrPlaneswalkerEffect targetDamage = new DealDamageToTargetPlayerOrPlaneswalkerEffect(new Fixed(4));
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"
        )).bindEffect(targetDamage);
        addSimultaneousSpellEffects(targetDamage, DealDamageToPermanentsTargetControlsEffect.toCreaturesOfTargetedPlayer(new Fixed(4)));
    }
}
