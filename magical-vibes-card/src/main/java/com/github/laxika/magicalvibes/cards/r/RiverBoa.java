package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "249")
public class RiverBoa extends Card {

    public RiverBoa() {
        // Islandwalk is loaded from Scryfall metadata; no engine logic needed.
        // {G}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate River Boa."));
    }
}
