package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "5ED", collectorNumber = "213")
public class BrassclawOrcs extends Card {

    public BrassclawOrcs() {
        // "This creature can't block creatures with power 2 or greater." Modelled as the exact
        // complement (mirror of Sunweb): it can only block attackers with power 1 or less.
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentPowerAtMostPredicate(1),
                "creatures with power 1 or less"
        ));
    }
}
