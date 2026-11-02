package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "162")
@CardRegistration(set = "DDS", collectorNumber = "53")
@CardRegistration(set = "TSR", collectorNumber = "259")
@CardRegistration(set = "DMR", collectorNumber = "196")
public class RadhaHeirToKeld extends Card {

    public RadhaHeirToKeld() {
        addTrigger(Triggers.attacks(), new MayEffect(
                new AwardManaEffect(ManaColor.RED, 2), "Add {R}{R}?"));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
