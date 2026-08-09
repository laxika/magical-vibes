package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "27")
public class BlizzardElemental extends Card {

    public BlizzardElemental() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{3}{U}: Untap this creature."
        ));
    }
}
