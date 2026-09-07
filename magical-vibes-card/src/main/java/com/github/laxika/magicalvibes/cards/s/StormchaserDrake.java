package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;

@CardRegistration(set = "VOW", collectorNumber = "82")
public class StormchaserDrake extends Card {

    public StormchaserDrake() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL,
                new TriggeringSpellControllerConditionalEffect(new DrawCardEffect()));
    }
}
