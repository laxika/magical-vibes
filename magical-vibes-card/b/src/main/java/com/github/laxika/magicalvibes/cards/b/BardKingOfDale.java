package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawExceptFirstDrawStepDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;

@CardRegistration(set = "HOB", collectorNumber = "144")
@CardRegistration(set = "HOC", collectorNumber = "144")
public class BardKingOfDale extends Card {

    public BardKingOfDale() {
        addStaticEffect(new DoubleDrawExceptFirstDrawStepDrawEffect());
        addStaticEffect(new MultiplyTokenCreationEffect(2));
    }
}
