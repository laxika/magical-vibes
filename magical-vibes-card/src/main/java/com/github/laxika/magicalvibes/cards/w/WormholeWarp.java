package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndMayCastRandomSideboardCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MB2", collectorNumber = "333")
@CardRegistration(set = "MB2", collectorNumber = "570")
public class WormholeWarp extends Card {

    public WormholeWarp() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new ExileTargetCreatureAndMayCastRandomSideboardCardEffect());
    }
}
