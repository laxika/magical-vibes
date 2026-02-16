package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactFromGraveyardToHandEffect;

public class TreasureHunter extends Card {

    public TreasureHunter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new ReturnArtifactFromGraveyardToHandEffect(), "Return an artifact from your graveyard to your hand?"));
    }
}
