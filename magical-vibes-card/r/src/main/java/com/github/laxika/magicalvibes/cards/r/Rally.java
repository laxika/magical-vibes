package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

@CardRegistration(set = "ICE", collectorNumber = "48")
public class Rally extends Card {

    public Rally() {
        // Blocking creatures get +1/+1 until end of turn.
        addSpellEffect(BoostEffect.allCreatures(1, 1, new PermanentIsBlockingPredicate()));
    }
}
