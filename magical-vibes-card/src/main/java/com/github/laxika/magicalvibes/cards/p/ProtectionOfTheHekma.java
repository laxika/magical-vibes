package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;

@CardRegistration(set = "AKH", collectorNumber = "23")
public class ProtectionOfTheHekma extends Card {

    public ProtectionOfTheHekma() {
        addEffect(EffectSlot.STATIC, new PreventDamageFromOpponentSourcesEffect(1));
    }
}
