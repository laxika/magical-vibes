package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;

public class BlackPantherHopeEnduring extends Card {

    public BlackPantherHopeEnduring() {
        addEffect(EffectSlot.STATIC, new PreventAllDamageEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));
    }
}
