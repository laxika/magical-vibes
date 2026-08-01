package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSelfEffect;

import java.util.List;

/**
 * Rainbow Efreet — {3}{U} Creature — Efreet 3/1.
 * Flying
 * "{U}{U}: This creature phases out."
 */
@CardRegistration(set = "VIS", collectorNumber = "41")
public class RainbowEfreet extends Card {

    public RainbowEfreet() {
        // Flying is auto-loaded from Scryfall and handled by the engine.
        addActivatedAbility(new ActivatedAbility(false, "{U}{U}",
                List.of(new PhaseOutSelfEffect()),
                "{U}{U}: This creature phases out."));
    }
}
