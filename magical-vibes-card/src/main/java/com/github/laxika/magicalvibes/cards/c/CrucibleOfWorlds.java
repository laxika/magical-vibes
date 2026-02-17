package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;

public class CrucibleOfWorlds extends Card {

    public CrucibleOfWorlds() {
        addEffect(EffectSlot.STATIC, new PlayLandsFromGraveyardEffect());
    }
}
