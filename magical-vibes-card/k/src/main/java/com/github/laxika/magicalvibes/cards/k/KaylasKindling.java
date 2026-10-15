package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "9")
public class KaylasKindling extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Abrade",
            "Cleansing Wildfire",
            "Terror of the Peaks",
            "Explosive Singularity",
            "Guttersnipe",
            "Seasoned Pyromancer",
            "Unexpected Windfall",
            "Banefire",
            "Lightning Bolt",
            "Dualcaster Mage",
            "Electrodominance",
            "Crackle with Power",
            "Volcanic Fallout",
            "Young Pyromancer",
            "Siege-Gang Commander");

    public KaylasKindling() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DraftCardFromSpellbookEffect(SPELLBOOK, true));
    }
}
