package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "135")
public class BurningSunsAvatar extends Card {

    public BurningSunsAvatar() {
        setAllowSharedTargets(true);

        // Deals 3 damage to target opponent or planeswalker
        DealDamageToTargetPlayerOrPlaneswalkerEffect opponentDamage =
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(3, PlayerRelation.OPPONENT);
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker"
        )).bindEffect(opponentDamage);

        // Deals 3 damage to up to one target creature
        DealDamageToTargetCreatureEffect creatureDamage = new DealDamageToTargetCreatureEffect(3);
        target(TargetFilters.creature(), 0, 1).bindEffect(creatureDamage);
        addSimultaneousEffects(EffectSlot.ON_ENTER_BATTLEFIELD, opponentDamage, creatureDamage);
    }
}
