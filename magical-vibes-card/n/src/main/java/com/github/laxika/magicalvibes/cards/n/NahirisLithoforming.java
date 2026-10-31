package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
        addSpellEffect(new SacrificePermanentsEffect(
                new XValue(), new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER)
                .withRecordedSacrificeCount());
        addSpellEffect(new DrawCardEffect(new EventValue()));
        addSpellEffect(new PlayAdditionalLandsEffect(new XValue()));
        addSpellEffect(new PermanentsEnterTappedThisTurnEffect(new PermanentIsLandPredicate()));
    }
}
