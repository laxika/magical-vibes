package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "SHM", collectorNumber = "167")
public class InkfathomInfiltrator extends Card {

    public InkfathomInfiltrator() {
        // This creature can't block...
        addStaticEffect(new CantBlockEffect());

        // ...and can't be blocked.
        addStaticEffect(new CantBeBlockedEffect());
    }
}
