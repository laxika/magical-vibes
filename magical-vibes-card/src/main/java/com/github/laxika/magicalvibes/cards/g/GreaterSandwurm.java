package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;


@CardRegistration(set = "AKH", collectorNumber = "168")
@CardRegistration(set = "AKR", collectorNumber = "192")
public class GreaterSandwurm extends Card {

    public GreaterSandwurm() {
        // "This creature can't be blocked by creatures with power 2 or less."
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtMostPredicate(2)));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
