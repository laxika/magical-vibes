package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;

public class AncestorsChosen extends Card {

    public AncestorsChosen() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifePerGraveyardCardEffect());
    }
}
