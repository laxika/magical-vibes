package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "152")
@CardRegistration(set = "MSC", collectorNumber = "339")
public class QuantumMisalignment extends Card {

    public QuantumMisalignment() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL,
                        CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                                List.of(), Set.of(), null, null, Map.of()));
    }
}
