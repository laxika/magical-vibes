package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;

public class LordOfThePit extends Card {

    public LordOfThePit() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeOtherCreatureOrDamageEffect(7));
    }
}
