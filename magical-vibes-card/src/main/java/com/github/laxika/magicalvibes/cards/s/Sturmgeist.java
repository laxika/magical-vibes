package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;

@CardRegistration(set = "ISD", collectorNumber = "82")
public class Sturmgeist extends Card {

    public Sturmgeist() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToCardsInHandEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect());
    }
}
