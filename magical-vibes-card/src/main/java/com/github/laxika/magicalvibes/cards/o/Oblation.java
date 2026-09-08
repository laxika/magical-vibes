package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "46")
public class Oblation extends Card {

    public Oblation() {
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.SPELL,
                new ShuffleTargetPermanentIntoLibraryEffect(new DrawCardEffect(2), ThenEffectRecipient.TARGET_OWNER));
    }
}
