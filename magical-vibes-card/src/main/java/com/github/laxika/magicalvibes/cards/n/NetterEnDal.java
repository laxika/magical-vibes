package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "13")
public class NetterEnDal extends Card {

    public NetterEnDal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new LockTargetPermanentEffect(true, false, false, EffectDuration.UNTIL_END_OF_TURN)
                ),
                "{W}, {T}, Discard a card: Target creature can't attack this turn.",
                TargetFilters.creature()
        ));
    }
}
