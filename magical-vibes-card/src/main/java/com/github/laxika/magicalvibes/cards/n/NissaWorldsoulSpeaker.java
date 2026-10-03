package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "DRC", collectorNumber = "13")
@CardRegistration(set = "DRC", collectorNumber = "29")
public class NissaWorldsoulSpeaker extends Card {

    public NissaWorldsoulSpeaker() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new EnergyCountersEffect(2));
        addEffect(EffectSlot.STATIC,
                AlternativeCostForSpellsEffect.payEnergy(8, new CardIsPermanentPredicate()));
    }
}
