package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "KLD", collectorNumber = "117")
@CardRegistration(set = "KLR", collectorNumber = "127")
public class HarnessedLightning extends Card {

    public HarnessedLightning() {
        target(TargetFilters.creature())
                .addSpellEffect(new EnergyCountersEffect(3))
                .addSpellEffect(EachPlayerPaysAnyManaEffect.byYou(Resource.ENERGY))
                .addSpellEffect(DealDamageEffect.toTargetCreature(AmountThisWay.energyPaid()));
    }
}
