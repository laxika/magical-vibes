package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "MIR", collectorNumber = "38")
public class Shadowbane extends Card {

    public Shadowbane() {
        addEffect(EffectSlot.SPELL, PreventDamageFromChosenSourceEffect.nextDamageToYouAndYourCreatures());
    }
}
