package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;

@CardRegistration(set = "ISD", collectorNumber = "17")
public class GeistHonoredMonk extends Card {

    public GeistHonoredMonk() {
        // Geist-Honored Monk's power and toughness are each equal to the number of creatures you control.
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControlledCreatureCountEffect());

        // When Geist-Honored Monk enters the battlefield, create two 1/1 white Spirit creature tokens with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSpirit(2));
    }
}
