package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndCreateTokenCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureThenCreateTokenCopiesWithHalfPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "1755")
@CardRegistration(set = "MAR", collectorNumber = "21")
@CardRegistration(set = "OMB", collectorNumber = "21")
public class SawInHalf extends Card {

    public SawInHalf() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetCreatureAndCreateTokenCopiesEffect());
    }
}
