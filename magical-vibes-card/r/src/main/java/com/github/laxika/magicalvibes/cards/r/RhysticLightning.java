package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

@CardRegistration(set = "PCY", collectorNumber = "99")
public class RhysticLightning extends Card {

    public RhysticLightning() {
        addSpellEffect(new MayPayManaEffect(
                "{2}",
                DealDamageEffect.toAnyTarget(2),
                "Pay {2} to reduce the damage to 2?",
                MayPayPayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER,
                DealDamageEffect.toAnyTarget(4),
                0
        ));
    }
}
