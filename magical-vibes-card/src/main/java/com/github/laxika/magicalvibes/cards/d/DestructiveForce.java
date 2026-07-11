package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "M11", collectorNumber = "133")
public class DestructiveForce extends Card {

    public DestructiveForce() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                5, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(5));
    }
}
