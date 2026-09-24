package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "291")
public class SerpentOfYawningDepths extends Card {

    public SerpentOfYawningDepths() {
        // Krakens, Leviathans, Octopuses, and Serpents you control can't be blocked except by
        // Krakens, Leviathans, Octopuses, and Serpents.
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.KRAKEN,
                        CardSubtype.LEVIATHAN,
                        CardSubtype.OCTOPUS,
                        CardSubtype.SERPENT)),
                "Krakens, Leviathans, Octopuses, and Serpents"));
    }
}
