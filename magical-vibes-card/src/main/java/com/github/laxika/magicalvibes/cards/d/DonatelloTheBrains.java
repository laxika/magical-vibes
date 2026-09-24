package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddMutagenTokenToTokenCreationEffect;

@CardRegistration(set = "TMC", collectorNumber = "2")
@CardRegistration(set = "TMC", collectorNumber = "85")
public class DonatelloTheBrains extends Card {

    public DonatelloTheBrains() {
        addEffect(EffectSlot.STATIC, new AddMutagenTokenToTokenCreationEffect());
    }
}
