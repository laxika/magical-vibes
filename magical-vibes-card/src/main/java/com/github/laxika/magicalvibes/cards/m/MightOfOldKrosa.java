package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "204")
public class MightOfOldKrosa extends Card {

    public MightOfOldKrosa() {
        // Target creature gets +2/+2 until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2))
                // If you cast this spell during your main phase, that creature gets +4/+4 instead.
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new CastDuringMainPhase(), new BoostTargetCreatureEffect(2, 2)));
    }
}
