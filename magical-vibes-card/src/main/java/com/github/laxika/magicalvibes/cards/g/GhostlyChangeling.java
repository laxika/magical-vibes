package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "116")
public class GhostlyChangeling extends Card {

    public GhostlyChangeling() {
        // Changeling is auto-loaded from Scryfall metadata.
        // {1}{B}: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new BoostSelfEffect(1, 1)),
                "{1}{B}: Ghostly Changeling gets +1/+1 until end of turn."
        ));
    }
}
