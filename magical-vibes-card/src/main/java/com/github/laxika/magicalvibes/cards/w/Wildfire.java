package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "9ED", collectorNumber = "228")
@CardRegistration(set = "P02", collectorNumber = "120")
public class Wildfire extends Card {

    public Wildfire() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                4, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(4));
    }
}
