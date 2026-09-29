package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToGrantIndestructibleToOwnCreaturesWithEqualPowerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M3C", collectorNumber = "41")
@CardRegistration(set = "M3C", collectorNumber = "93")
public class LocalizedDestruction extends Card {

    public LocalizedDestruction() {
        addEffect(EffectSlot.SPELL, new EnergyCountersEffect(1));
        addEffect(EffectSlot.SPELL,
                new PayAnyAmountOfEnergyToGrantIndestructibleToOwnCreaturesWithEqualPowerEffect());
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
