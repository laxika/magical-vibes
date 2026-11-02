package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FreeCyclingFirstCardEachTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C20", collectorNumber = "7")
public class GaviNestWarden extends Card {

    public GaviNestWarden() {
        addStaticEffect(new FreeCyclingFirstCardEachTurnEffect());
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new CreateTokenEffect("Dinosaur Cat", 2, 2, CardColor.RED,
                        Set.of(CardColor.RED, CardColor.WHITE),
                        List.of(CardSubtype.DINOSAUR, CardSubtype.CAT)));
    }
}
