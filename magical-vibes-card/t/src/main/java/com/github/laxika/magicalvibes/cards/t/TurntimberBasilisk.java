package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "190")
@CardRegistration(set = "DDP", collectorNumber = "25")
@CardRegistration(set = "E01", collectorNumber = "76")
public class TurntimberBasilisk extends Card {

    public TurntimberBasilisk() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new MayEffect(new MustBlockSourceEffect(null),
                                "Have target creature block Turntimber Basilisk this turn if able?"));
    }
}
