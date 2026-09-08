package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "85")
public class ScarwoodBandits extends Card {

    public ScarwoodBandits() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new MayPayManaEffect(
                        "{2}",
                        null,
                        "Pay {2} to keep control of the target artifact?",
                        MayPayPayer.TARGET_PERMANENT_CONTROLLER,
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS),
                        0
                )),
                "{2}{G}, {T}: Unless an opponent pays {2}, gain control of target artifact for as long as Scarwood Bandits remains on the battlefield.",
                TargetFilters.artifact()
        ));
    }
}
