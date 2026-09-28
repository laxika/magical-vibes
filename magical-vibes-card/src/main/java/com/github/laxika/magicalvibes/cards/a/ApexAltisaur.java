package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCC", collectorNumber = "232")
public class ApexAltisaur extends Card {

    public ApexAltisaur() {
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SourceFightsTargetCreatureEffect())
                .addEffect(EffectSlot.ON_DEALT_DAMAGE, new SourceFightsTargetCreatureEffect());
    }
}
