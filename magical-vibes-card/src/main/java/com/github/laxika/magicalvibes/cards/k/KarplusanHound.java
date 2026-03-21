package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "DOM", collectorNumber = "277")
public class KarplusanHound extends Card {

    public KarplusanHound() {
        // Whenever Karplusan Hound attacks, if you control a Chandra planeswalker,
        // this creature deals 2 damage to any target.
        addEffect(EffectSlot.ON_ATTACK,
                new ControlsSubtypeConditionalEffect(CardSubtype.CHANDRA,
                        new DealDamageToAnyTargetEffect(2, false)));
    }
}
