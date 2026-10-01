package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalBeginningPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.ReverseTurnOrderEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WHO", collectorNumber = "602")
public class TempleOfAtropos extends Card {

    public TempleOfAtropos() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new AdditionalBeginningPhaseEffect(true));
        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new ReverseTurnOrderEffect(),
                new PlaneswalkEffect()));
    }
}
