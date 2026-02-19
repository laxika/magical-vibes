package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEffect;

@CardRegistration(set = "10E", collectorNumber = "225")
public class RelentlessAssault extends Card {

    public RelentlessAssault() {
        addEffect(EffectSlot.SPELL, new UntapAttackedCreaturesEffect());
        addEffect(EffectSlot.SPELL, new AdditionalCombatMainPhaseEffect(1));
    }
}
