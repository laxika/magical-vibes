package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "XLN", collectorNumber = "135")
public class BurningSunsAvatar extends Card {

    public BurningSunsAvatar() {
        setAllowSharedTargets(true);

        // Deals 3 damage to target opponent or planeswalker
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be an opponent or planeswalker"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetOpponentOrPlaneswalkerEffect(3));

        // Deals 3 damage to up to one target creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ), 0, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(3));
    }
}
