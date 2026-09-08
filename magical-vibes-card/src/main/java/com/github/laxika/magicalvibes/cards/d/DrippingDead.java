package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureEffect;

@CardRegistration(set = "LGN", collectorNumber = "67")
public class DrippingDead extends Card {

    public DrippingDead() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new DestroyDamagedCreatureEffect(true, true));
    }
}
