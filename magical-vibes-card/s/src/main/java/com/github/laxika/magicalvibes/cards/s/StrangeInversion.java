package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

@CardRegistration(set = "CHK", collectorNumber = "192")
public class StrangeInversion extends Card {

    public StrangeInversion() {
        addEffect(EffectSlot.SPELL, new SwitchPowerToughnessEffect());
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{1}{R}"));
    }
}
