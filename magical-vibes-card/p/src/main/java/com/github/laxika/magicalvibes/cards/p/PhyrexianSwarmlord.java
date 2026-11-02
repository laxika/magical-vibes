package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.OpponentPoisonCounters;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "119")
@CardRegistration(set = "ONC", collectorNumber = "111")
public class PhyrexianSwarmlord extends Card {

    public PhyrexianSwarmlord() {
        // At the beginning of your upkeep, create a 1/1 green Phyrexian Insect creature
        // token with infect for each poison counter your opponents have.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CreateTokenEffect(
                new OpponentPoisonCounters(),
                "Phyrexian Insect",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.INSECT),
                Set.of(Keyword.INFECT),
                Set.of()
        ));
    }
}
