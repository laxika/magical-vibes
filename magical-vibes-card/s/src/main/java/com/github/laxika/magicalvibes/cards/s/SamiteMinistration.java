package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "INV", collectorNumber = "36")
public class SamiteMinistration extends Card {

    public SamiteMinistration() {
        addEffect(EffectSlot.SPELL,
                PreventDamageFromChosenSourceEffect.allDamageToYouAndGainLifeForBlackOrRedSource());
    }
}
