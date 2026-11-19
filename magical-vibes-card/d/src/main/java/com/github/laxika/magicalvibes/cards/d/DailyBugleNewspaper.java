package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MSC", collectorNumber = "749")
public class DailyBugleNewspaper extends Card {

    public DailyBugleNewspaper() {
        // {2}, {T}: Draw a card, then discard a card. Create a Treasure token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, Subjects.you()),
                        CreateTokenEffect.ofTreasureToken(1)
                ),
                "{2}, {T}: Draw a card, then discard a card. Create a Treasure token."
        ));
    }
}
