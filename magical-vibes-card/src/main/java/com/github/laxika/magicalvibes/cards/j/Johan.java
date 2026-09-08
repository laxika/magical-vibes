package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.JohanCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "CHR", collectorNumber = "77")
@CardRegistration(set = "LEG", collectorNumber = "236")
public class Johan extends Card {

    public Johan() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MayEffect(new JohanCombatEffect(),
                        "Have Johan not attack so your creatures don't tap this combat?"));
    }
}
