package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "62")
public class Despoil extends Card {

    public Despoil() {
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                new LoseLifeEffect(2), ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
