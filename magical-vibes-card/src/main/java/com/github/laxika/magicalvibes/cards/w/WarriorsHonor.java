package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

public class WarriorsHonor extends Card {

    public WarriorsHonor() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
