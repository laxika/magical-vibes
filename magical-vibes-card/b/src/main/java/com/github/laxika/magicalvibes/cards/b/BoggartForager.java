package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "154")
public class BoggartForager extends Card {

    public BoggartForager() {
        // {R}, Sacrifice Boggart Forager: Target player shuffles their library.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new ShuffleLibraryEffect(true)),
                "{R}, Sacrifice Boggart Forager: Target player shuffles their library."
        ));
    }
}
