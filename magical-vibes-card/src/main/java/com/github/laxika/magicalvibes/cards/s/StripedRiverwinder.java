package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "48")
public class StripedRiverwinder extends Card {

    public StripedRiverwinder() {
        // Hexproof is auto-loaded from Scryfall metadata.

        // Cycling {U} ({U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new DrawCardEffect(1)),
                "Cycling {U} ({U}, Discard this card: Draw a card.)"));
    }
}
