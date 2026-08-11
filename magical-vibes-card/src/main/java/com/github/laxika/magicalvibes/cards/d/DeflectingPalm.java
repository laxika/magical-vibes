package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "KTK", collectorNumber = "173")
public class DeflectingPalm extends Card {

    public DeflectingPalm() {
        addEffect(EffectSlot.SPELL,
                PreventDamageFromChosenSourceEffect.nextDamageToYouAndDamageSourceController());
    }
}
