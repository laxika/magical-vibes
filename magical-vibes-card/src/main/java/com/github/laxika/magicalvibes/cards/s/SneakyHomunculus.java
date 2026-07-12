package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "101")
public class SneakyHomunculus extends Card {

    public SneakyHomunculus() {
        // "This creature can't block or be blocked by creatures with power 2 or greater."
        // Can't be blocked by power >= 2:
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtLeastPredicate(2)
        ));
        // Can't block power >= 2, modelled as the exact complement: can only block power <= 1.
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentPowerAtMostPredicate(1),
                "creatures with power 1 or less"
        ));
    }
}
