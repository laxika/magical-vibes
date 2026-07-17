package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaDealXDamageToAnyTargetEffect;

@CardRegistration(set = "ALA", collectorNumber = "100")
public class FlameblastDragon extends Card {

    public FlameblastDragon() {
        // Whenever this creature attacks, you may pay {X}{R}. If you do, it deals X damage to any target.
        addEffect(EffectSlot.ON_ATTACK, new PayXManaDealXDamageToAnyTargetEffect("{X}{R}"));
    }
}
