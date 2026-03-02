package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "MBS", collectorNumber = "103")
public class CoreProwler extends Card {

    public CoreProwler() {
        // When Core Prowler dies, proliferate.
        addEffect(EffectSlot.ON_DEATH, new ProliferateEffect());
    }
}
