package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledSubtypeCountEffect;

@CardRegistration(set = "10E", collectorNumber = "164")
public class Nightmare extends Card {

    public Nightmare() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControlledSubtypeCountEffect(CardSubtype.SWAMP));
    }
}
