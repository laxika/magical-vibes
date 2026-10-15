package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect;

/** Back face of Garland, Knight of Cornelia. */
public class ChaosTheEndless extends Card {

    public ChaosTheEndless() {
        addEffect(EffectSlot.ON_DEATH, new PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect());
    }
}
