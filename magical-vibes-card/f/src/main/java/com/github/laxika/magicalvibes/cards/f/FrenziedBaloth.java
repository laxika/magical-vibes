package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCreatureSpellsCantBeCounteredEffect;

@CardRegistration(set = "EOE", collectorNumber = "183")
public class FrenziedBaloth extends Card {

    public FrenziedBaloth() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new ControllerCreatureSpellsCantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new CombatDamageCantBePreventedEffect());
    }
}
