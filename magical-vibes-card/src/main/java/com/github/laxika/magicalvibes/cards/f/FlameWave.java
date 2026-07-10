package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "9ED", collectorNumber = "182")
public class FlameWave extends Card {

    public FlameWave() {
        // Flame Wave deals 4 damage to target player or planeswalker and each creature
        // that player or that planeswalker's controller controls.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a player or planeswalker"
        )).addEffect(EffectSlot.SPELL, new DealDamageToTargetAndTheirCreaturesEffect(4));
    }
}
