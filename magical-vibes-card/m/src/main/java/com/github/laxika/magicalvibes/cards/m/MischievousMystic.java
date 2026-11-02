package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "47")
public class MischievousMystic extends Card {

    public MischievousMystic() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new CreateTokenEffect("Faerie", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.FAERIE), Set.of(Keyword.FLYING), Set.of()));
    }
}
