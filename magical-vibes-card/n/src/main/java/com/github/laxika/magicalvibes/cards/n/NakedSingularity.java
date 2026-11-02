package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.NakedSingularityManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "330")
@CardRegistration(set = "ME4", collectorNumber = "216")
public class NakedSingularity extends Card {

    public NakedSingularity() {
        // Cumulative upkeep {3}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{3}"));

        // If tapped for mana, Plains produce {R}, Islands produce {G}, Swamps produce {W},
        // Mountains produce {U}, and Forests produce {B} instead of any other type.
        addStaticEffect(new NakedSingularityManaEffect());
    }
}
