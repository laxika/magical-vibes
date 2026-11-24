package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.AddTokenCreationEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "TMC", collectorNumber = "2")
@CardRegistration(set = "TMC", collectorNumber = "85")
public class DonatelloTheBrains extends Card {

    public DonatelloTheBrains() {
        addStaticEffect(AddTokenCreationEffect.plus(AddTokenCreationEffect.MUTAGEN, AddTokenCreationEffect.TokenKind.any()));
    }
}
