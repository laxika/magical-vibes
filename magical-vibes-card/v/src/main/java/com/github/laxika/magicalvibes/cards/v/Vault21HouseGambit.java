package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "69")
@CardRegistration(set = "PIP", collectorNumber = "597")
public class Vault21HouseGambit extends Card {

    public Vault21HouseGambit() {
        addTrigger(Triggers.chapter(1), DiscardAndDrawCardEffect.thenDraw(1, 1));
        addTrigger(Triggers.chapter(2), DiscardAndDrawCardEffect.thenDraw(1, 1));
        addTrigger(Triggers.chapter(3),
                new RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffect());
    }
}
