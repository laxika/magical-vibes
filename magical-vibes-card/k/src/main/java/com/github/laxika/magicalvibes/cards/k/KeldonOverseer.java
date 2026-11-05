package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "134")
public class KeldonOverseer extends Card {

    public KeldonOverseer() {
        // Kicker {3}{R}
        addStaticEffect(new KickerEffect("{3}{R}"));

        // When this creature enters, if it was kicked, gain control of target creature
        // until end of turn. Untap that creature. It gains haste until end of turn.
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
