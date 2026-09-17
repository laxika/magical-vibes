package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SOI", collectorNumber = "60")
@CardRegistration(set = "SIR", collectorNumber = "66")
public class ErdwalIlluminator extends Card {

    public ErdwalIlluminator() {
        addEffect(EffectSlot.ON_CONTROLLER_INVESTIGATES, CreateTokenEffect.ofClueToken(1));
    }
}
