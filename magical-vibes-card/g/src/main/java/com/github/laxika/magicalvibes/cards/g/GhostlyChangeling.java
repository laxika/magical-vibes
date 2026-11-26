package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "116")
@CardRegistration(set = "MM2", collectorNumber = "83")
public class GhostlyChangeling extends Card {

    public GhostlyChangeling() {
        // Changeling is auto-loaded from Scryfall metadata.
        // {1}{B}: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(BoostEffect.self(1, 1)),
                "{1}{B}: Ghostly Changeling gets +1/+1 until end of turn."
        ));
    }
}
