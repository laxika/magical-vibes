package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "113")
public class MarrowBats extends Card {

    public MarrowBats() {
        // Flying is auto-loaded from Scryfall keywords.

        // Pay 4 life: Regenerate Marrow Bats.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(4), new RegenerateEffect()),
                "Pay 4 life: Regenerate Marrow Bats."));
    }
}
