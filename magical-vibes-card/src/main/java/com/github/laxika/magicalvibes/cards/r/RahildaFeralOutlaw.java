package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileRandomNonlandCardFromDamagedPlayerLibraryEffect;

public class RahildaFeralOutlaw extends Card {

    public RahildaFeralOutlaw() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileRandomNonlandCardFromDamagedPlayerLibraryEffect());
        addEffect(EffectSlot.STATIC, RahildaWantedCutthroat.castExiledNonlandCards());
    }
}
