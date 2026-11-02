package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "130")
public class MadRatter extends Card {

    public MadRatter() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new CreateTokenEffect(2, "Rat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.RAT), Set.of(), Set.of()));
    }
}
