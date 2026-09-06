package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "DTK", collectorNumber = "21")
public class GreatTeachersDecree extends Card {

    public GreatTeachersDecree() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 1));
    }
}
