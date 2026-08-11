package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "71")
public class TritonTactics extends Card {

    public TritonTactics() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(0, 3))
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS))
                .addEffect(EffectSlot.SPELL, new TapCombatOpponentsOfTargetAtEndOfCombatEffect());
    }
}
