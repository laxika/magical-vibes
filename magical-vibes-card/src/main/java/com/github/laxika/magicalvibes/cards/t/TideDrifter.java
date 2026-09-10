package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;

@CardRegistration(set = "BFZ", collectorNumber = "67")
public class TideDrifter extends Card {

    public TideDrifter() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES, new PermanentIsColorlessPredicate()));
    }
}
