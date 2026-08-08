package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "BOK", collectorNumber = "98")
public class CrackTheEarth extends Card {

    public CrackTheEarth() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                1, new PermanentTruePredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
