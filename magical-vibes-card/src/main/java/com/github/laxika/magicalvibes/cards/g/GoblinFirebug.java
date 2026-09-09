package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "LGN", collectorNumber = "98")
public class GoblinFirebug extends Card {

    public GoblinFirebug() {
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
    }
}
