package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "38")
public class BurrogBefuddler extends Card {

    public BurrogBefuddler() {
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostTargetCreatureEffect(-1, 0));
    }
}
