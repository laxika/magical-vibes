package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "112")
public class BlightMamba extends Card {

    public BlightMamba() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new RegenerateEffect()),
                "{1}{G}: Regenerate Blight Mamba."
        ));
    }
}
