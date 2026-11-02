package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "314")
@CardRegistration(set = "INR", collectorNumber = "183")
@CardRegistration(set = "AVR", collectorNumber = "166")
@CardRegistration(set = "MM3", collectorNumber = "116")
@CardRegistration(set = "TSR", collectorNumber = "354")
@CardRegistration(set = "SIS", collectorNumber = "47")
@CardRegistration(set = "LTC", collectorNumber = "231")
public class ZealousConscripts extends Card {

    public ZealousConscripts() {
        // When this creature enters, gain control of target permanent until end of turn.
        // Untap that permanent. It gains haste until end of turn.
        target(TargetFilters.permanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
