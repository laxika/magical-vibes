package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UsePowerForLethalDamageEffect;

@CardRegistration(set = "CMM", collectorNumber = "366")
@CardRegistration(set = "CMM", collectorNumber = "599")
public class ZilorthaStrengthIncarnate extends Card {

    public ZilorthaStrengthIncarnate() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new UsePowerForLethalDamageEffect(), GrantScope.ALL_OWN_CREATURES));
    }
}
