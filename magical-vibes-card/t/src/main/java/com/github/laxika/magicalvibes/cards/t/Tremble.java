package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ODY", collectorNumber = "225")
public class Tremble extends Card {

    public Tremble() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
