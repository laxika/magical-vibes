package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "280")
public class YavimayaGnats extends Card {

    public YavimayaGnats() {
        // Flying is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new RegenerateEffect()), "{G}: Regenerate Yavimaya Gnats."));
    }
}
