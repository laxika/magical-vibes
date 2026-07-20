package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "39")
public class WingedShepherd extends Card {

    public WingedShepherd() {
        // Flying and vigilance are intrinsic keywords (auto-loaded from Scryfall).
        // Cycling {W} ({W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new DrawCardEffect(1)),
                "Cycling {W} ({W}, Discard this card: Draw a card.)"));
    }
}
