package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

/**
 * Cyclops Tyrant — intimidate (auto-loaded keyword) plus a blocking restriction:
 * it can't block creatures with power 2 or less, i.e. it may block only attackers
 * with power 3 or greater.
 */
@CardRegistration(set = "M14", collectorNumber = "135")
public class CyclopsTyrant extends Card {

    public CyclopsTyrant() {
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentPowerAtLeastPredicate(3),
                "creatures with power 3 or greater"
        ));
    }
}
