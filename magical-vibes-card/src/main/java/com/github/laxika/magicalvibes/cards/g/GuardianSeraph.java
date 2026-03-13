package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;

@CardRegistration(set = "M10", collectorNumber = "13")
public class GuardianSeraph extends Card {

    public GuardianSeraph() {
        addEffect(EffectSlot.STATIC, new PreventDamageFromOpponentSourcesEffect(1));
    }
}
