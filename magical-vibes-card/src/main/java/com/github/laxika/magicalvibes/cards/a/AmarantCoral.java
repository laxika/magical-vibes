package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "FIC", collectorNumber = "457")
public class AmarantCoral extends Card {

    public AmarantCoral() {
        // Amarant Coral attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // Whenever Amarant Coral deals combat damage to an opponent, it deals that much damage to
        // each other opponent.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DealDamageToEachOtherOpponentEffect());
    }
}
