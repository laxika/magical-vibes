package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsEnterTappedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ZNR", collectorNumber = "151")
public class NahirisLithoforming extends Card {

    public NahirisLithoforming() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new XValue(), new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER)
                .withRecordedSacrificeCount());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new EventValue()));
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new PermanentsEnterTappedThisTurnEffect(new PermanentIsLandPredicate()));
    }
}
