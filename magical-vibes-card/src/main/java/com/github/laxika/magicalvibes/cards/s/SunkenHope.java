package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BounceCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "112")
public class SunkenHope extends Card {

    public SunkenHope() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new BounceCreatureOnUpkeepEffect(
                BounceCreatureOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER,
                Set.of(),
                "Choose a creature you control to return to its owner's hand."
        ));
    }
}
