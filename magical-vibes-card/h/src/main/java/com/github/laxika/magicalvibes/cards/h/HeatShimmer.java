package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "175")
@CardRegistration(set = "SLD", collectorNumber = "1957")
@CardRegistration(set = "2XM", collectorNumber = "130")
@CardRegistration(set = "SPG", collectorNumber = "137")
public class HeatShimmer extends Card {

    public HeatShimmer() {
        // The token has haste and "At the beginning of the end step, exile this permanent" as part of
        // its copiable values, so it loses the exile ability with its other abilities and a copy of
        // the token inherits it.
        target(TargetFilters.creature())
                .addSpellEffect(new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(), null, null, Map.of(),
                        false, false, false, false,
                        false, false, null, Set.of(Keyword.HASTE),
                        false, List.of(LegacySlots.triggered(EffectSlot.END_STEP_TRIGGERED, List.<CardEffect>of(new ExileSelfEffect()))),
                        List.of(), false, false,
                        new Fixed(1), false, Set.of()));
    }
}
