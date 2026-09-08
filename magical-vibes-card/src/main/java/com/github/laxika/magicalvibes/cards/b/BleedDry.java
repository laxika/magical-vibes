package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "94")
public class BleedDry extends Card {

    public BleedDry() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-13, -13));
    }
}
