package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "52")
public class Sunweb extends Card {

    public Sunweb() {
        // "This creature can't block creatures with power 2 or less." Modelled as the exact
        // complement: it can only block attackers with power 3 or greater. (Flying/Defender are
        // keywords auto-loaded from oracle data.)
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentPowerAtLeastPredicate(3),
                "creatures with power 3 or greater"
        ));
    }
}
