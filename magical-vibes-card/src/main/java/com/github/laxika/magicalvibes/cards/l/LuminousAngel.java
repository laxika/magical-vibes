package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MRD", collectorNumber = "15")
public class LuminousAngel extends Card {

    public LuminousAngel() {
        // At the beginning of your upkeep, you may create a 1/1 white Spirit creature token with flying.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                CreateTokenEffect.whiteSpirit(1),
                "Create a 1/1 white Spirit creature token with flying?"));
    }
}
