package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "49")
public class CogworkWrestler extends Card {

    public CogworkWrestler() {
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostTargetCreatureEffect(-2, 0));
    }
}
