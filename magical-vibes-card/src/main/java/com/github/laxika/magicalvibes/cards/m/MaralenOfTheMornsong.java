package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersCannotDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndSearchesLibraryToHandEffect;

@CardRegistration(set = "MOR", collectorNumber = "65")
public class MaralenOfTheMornsong extends Card {

    public MaralenOfTheMornsong() {
        // Players can't draw cards.
        addEffect(EffectSlot.STATIC, new PlayersCannotDrawCardsEffect());
        // At the beginning of each player's draw step, that player loses 3 life, searches their
        // library for a card, puts it into their hand, then shuffles.
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new TargetPlayerLosesLifeAndSearchesLibraryToHandEffect(3));
    }
}
