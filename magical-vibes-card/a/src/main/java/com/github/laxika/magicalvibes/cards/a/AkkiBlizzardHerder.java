package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "BOK", collectorNumber = "91")
public class AkkiBlizzardHerder extends Card {

    public AkkiBlizzardHerder() {
        addEffect(EffectSlot.ON_DEATH, new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
