package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "137")
public class RageOfPurphoros extends Card {

    public RageOfPurphoros() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4))
                .addEffect(EffectSlot.SPELL, new PreventTargetCreatureRegenerationThisTurnEffect())
                .addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
