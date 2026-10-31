package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "RTR", collectorNumber = "85")
public class AnnihilatingFire extends Card {

    public AnnihilatingFire() {
        // 3 damage; a creature dealt damage this way that would die this turn is exiled instead.
        addSpellEffect(new DealDamageToAnyTargetEffect(new Fixed(3), false, true));
    }
}
