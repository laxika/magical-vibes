package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleCreaturesEnteringFromExileEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "569")
public class BadWolfBay extends Card {

    public BadWolfBay() {
        target(TargetFilters.creature(), 0, 1).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ExileTargetCreatureAndReturnAtNextEndStepEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new PlaneswalkEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new ShuffleCreaturesEnteringFromExileEffect());
    }
}
