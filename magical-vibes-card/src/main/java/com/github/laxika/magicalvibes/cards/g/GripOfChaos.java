package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReselectTargetAtRandomEffect;

@CardRegistration(set = "SCG", collectorNumber = "98")
public class GripOfChaos extends Card {

    public GripOfChaos() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CHOOSES_TARGETS, new ReselectTargetAtRandomEffect());
    }
}
