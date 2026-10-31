package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToGrantIndestructibleToOwnCreaturesWithEqualPowerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M3C", collectorNumber = "41")
@CardRegistration(set = "M3C", collectorNumber = "93")
public class LocalizedDestruction extends Card {

    public LocalizedDestruction() {
        addSpellEffect(new EnergyCountersEffect(1));
        addSpellEffect(new PayAnyAmountOfEnergyToGrantIndestructibleToOwnCreaturesWithEqualPowerEffect());
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
