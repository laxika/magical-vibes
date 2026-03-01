package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentEnteredThisTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOM", collectorNumber = "105")
public class TunnelIgnus extends Card {

    public TunnelIgnus() {
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new PermanentEnteredThisTurnConditionalEffect(
                        new DealDamageToTargetPlayerEffect(3),
                        new CardTypePredicate(CardType.LAND),
                        2
                ));
    }
}
