package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffect;

@CardRegistration(set = "ODY", collectorNumber = "88")
public class LaquatussCreativity extends Card {

    public LaquatussCreativity() {
        addSpellEffect(new TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffect());
    }
}
