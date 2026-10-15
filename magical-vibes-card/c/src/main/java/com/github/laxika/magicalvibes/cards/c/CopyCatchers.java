package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "MKC", collectorNumber = "20")
@CardRegistration(set = "MKC", collectorNumber = "330")
public class CopyCatchers extends Card {

    public CopyCatchers() {
        addEffect(EffectSlot.ON_CONTROLLER_SURVEILS,
                new MayPayManaEffect("{1}{U}", new CreateTokenCopyOfSourceEffect(),
                        "Pay {1}{U} to create a token that's a copy of Copy Catchers?"));
    }
}
