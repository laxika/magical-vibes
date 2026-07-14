package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlUntapAndHasteTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "EVE", collectorNumber = "102")
public class DominusOfFealty extends Card {

    public DominusOfFealty() {
        // At the beginning of your upkeep, you may gain control of target permanent until end of
        // turn. If you do, untap it and it gains haste until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new GainControlUntapAndHasteTargetEffect(),
                "Gain control of target permanent until end of turn?"
        ));
    }
}
