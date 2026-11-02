package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlayersCannotDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndSearchesLibraryToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "65")
public class MaralenOfTheMornsong extends Card {

    public MaralenOfTheMornsong() {
        // Players can't draw cards.
        addStaticEffect(new PlayersCannotDrawCardsEffect());
        // At the beginning of each player's draw step, that player loses 3 life, searches their
        // library for a card, puts it into their hand, then shuffles.
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new TargetPlayerLosesLifeAndSearchesLibraryToHandEffect(3));
    }
}
