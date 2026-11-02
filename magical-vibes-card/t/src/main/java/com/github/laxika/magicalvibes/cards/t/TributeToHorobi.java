package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EchoOfDeathsWail;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "124")
public class TributeToHorobi extends Card {

    public TributeToHorobi() {
        setBackFaceCard(new EchoOfDeathsWail());

        CreateTokenEffect ratToken = new CreateTokenEffect(
                "Rat", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.RAT, CardSubtype.ROGUE), Set.of(), Set.of());
        addTrigger(Triggers.chapter(1), new EachOpponentCreatesTokenEffect(ratToken));
        addTrigger(Triggers.chapter(2), new EachOpponentCreatesTokenEffect(ratToken));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "EchoOfDeathsWail";
    }
}
