package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;

@CardRegistration(set = "AFR", collectorNumber = "154")
public class MagicMissile extends Card {

    public MagicMissile() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.SPELL, new DealDividedDamageEffect(
                new Fixed(3), null, DivisionMode.CHOSEN, null, 3, true, false, false));
    }
}
