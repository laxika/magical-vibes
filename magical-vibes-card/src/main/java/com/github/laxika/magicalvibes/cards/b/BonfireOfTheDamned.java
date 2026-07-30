package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "AVR", collectorNumber = "129")
public class BonfireOfTheDamned extends Card {

    public BonfireOfTheDamned() {
        // Miracle {X}{R}
        addCastingOption(new MiracleCast("{X}{R}"));

        // Bonfire of the Damned deals X damage to target player or planeswalker and each
        // creature that player or that planeswalker's controller controls.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a player or planeswalker"
        )).addEffect(EffectSlot.SPELL, new DealDamageToTargetAndTheirCreaturesEffect(new XValue()));
    }
}
