package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GuardianAngelPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "SUM", collectorNumber = "21")
public class GuardianAngel extends Card {

    public GuardianAngel() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTarget(new XValue()));
        addEffect(EffectSlot.SPELL, new GuardianAngelPermissionEffect());
    }
}
