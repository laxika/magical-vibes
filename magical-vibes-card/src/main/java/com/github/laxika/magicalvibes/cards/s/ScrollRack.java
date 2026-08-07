package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScrollRackEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "308")
public class ScrollRack extends Card {

    public ScrollRack() {
        // {1}, {T}: Exile any number of cards from your hand face down. Put that many cards from the
        // top of your library into your hand. Then look at the exiled cards and put them on top of
        // your library in any order. The whole swap happens in one resolution, so the set-aside cards
        // never become publicly visible.
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new ScrollRackEffect()),
                "{1}, {T}: Exile any number of cards from your hand face down. Put that many cards from"
                        + " the top of your library into your hand. Then look at the exiled cards and put"
                        + " them on top of your library in any order."));
    }
}
