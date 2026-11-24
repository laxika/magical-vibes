package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AddTokenCreationEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HOC", collectorNumber = "4")
public class BilboFellowConspirator extends Card {

    public BilboFellowConspirator() {
        addStaticEffect(AddTokenCreationEffect.oneOfEach(AddTokenCreationEffect.TokenKind.withAnySubtype(CardSubtype.FOOD),
                AddTokenCreationEffect.FOOD, AddTokenCreationEffect.TREASURE));
    }
}
