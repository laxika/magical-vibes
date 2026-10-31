package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMN", collectorNumber = "84")
@CardRegistration(set = "W17", collectorNumber = "16")
@CardRegistration(set = "MB1", collectorNumber = "84")
@CardRegistration(set = "SIR", collectorNumber = "102")
public class CertainDeath extends Card {

    public CertainDeath() {
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER))
                .addSpellEffect(new GainLifeEffect(2));
    }
}
