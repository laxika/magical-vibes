package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "INV", collectorNumber = "243")
public class DarigaazTheIgniter extends Card {

    public DarigaazTheIgniter() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayPayManaEffect("{2}{R}", new DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect(),
                        "Pay {2}{R}?"));
    }
}
