package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "170")
public class GruulRagebeast extends Card {

    public GruulRagebeast() {
        // Whenever this creature or another creature you control enters, that creature fights
        // target creature an opponent controls.
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureFightsTargetCreatureEffect());
        target(TargetFilters.creatureAnOpponentControls());
    }
}
