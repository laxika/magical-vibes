package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToHandEffect;

public class Gravedigger extends Card {

    public Gravedigger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new ReturnCreatureFromGraveyardToHandEffect(), "Return a creature card from your graveyard to your hand?"));
    }
}
