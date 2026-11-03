package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "36")
public class ValiantRescuer extends Card {

    public ValiantRescuer() {
        addTrigger(Triggers.youCycleCard().firstTimeEachTurn(),
                new CreateTokenEffect("Human Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));
        addCycling("{2}");
    }
}
