package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "FRF", collectorNumber = "164")
public class UginsConstruct extends Card {

    public UginsConstruct() {
        // When this creature enters, sacrifice a permanent that's one or more colors.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificePermanentsEffect(
                1,
                new PermanentNotPredicate(new PermanentIsColorlessPredicate()),
                SacrificeRecipient.CONTROLLER));
    }
}
