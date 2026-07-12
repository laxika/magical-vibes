package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "8ED", collectorNumber = "19")
public class EliteJavelineer extends Card {

    public EliteJavelineer() {
        // "Whenever this creature blocks, it deals 1 damage to target attacking creature."
        // Unlike "that creature" block triggers (Ashmouth Hound), the controller chooses any
        // attacking creature, so the card carries a target filter that routes the block trigger
        // through the targeting pipeline (see CombatBlockService).
        target(new PermanentPredicateTargetFilter(
                new PermanentIsAttackingPredicate(),
                "Target must be an attacking creature"
        ))
                .addEffect(EffectSlot.ON_BLOCK, new DealDamageToTargetCreatureEffect(1));
    }
}
