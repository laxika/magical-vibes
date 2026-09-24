package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;


@CardRegistration(set = "CON", collectorNumber = "143")
@CardRegistration(set = "M13", collectorNumber = "227")
@CardRegistration(set = "M19", collectorNumber = "254")
@CardRegistration(set = "CMM", collectorNumber = "425")
@CardRegistration(set = "CMM", collectorNumber = "663")
@CardRegistration(set = "SLZ", collectorNumber = "120")
@CardRegistration(set = "SLZ", collectorNumber = "241")
@CardRegistration(set = "SLZ", collectorNumber = "362")
@CardRegistration(set = "C14", collectorNumber = "308")
@CardRegistration(set = "C15", collectorNumber = "301")
public class ReliquaryTower extends Card {

    public ReliquaryTower() {
        // You have no maximum hand size.
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
