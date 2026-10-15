package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "FUT", collectorNumber = "45")
public class UnblinkingBleb extends Card {

    public UnblinkingBleb() {
        addMorph("{2}{U}");
        addEffect(EffectSlot.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP,
                new MayEffect(new ScryEffect(2), "Scry 2?"));
    }
}
