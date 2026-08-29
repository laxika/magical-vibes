package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "40")
public class Oust extends Card {

    public Oust() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new PutTargetPermanentIntoLibraryNFromTopThenEffect(
                        1, new GainLifeEffect(3), ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
