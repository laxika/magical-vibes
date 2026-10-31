package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MKM", collectorNumber = "30")
@CardRegistration(set = "MKM", collectorNumber = "292")
public class OnTheJob extends Card {

    public OnTheJob() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 1));
        addSpellEffect(CreateTokenEffect.ofClueToken(1));
    }
}
