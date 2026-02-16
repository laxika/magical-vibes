package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

public class BeaconOfUnrest extends Card {

    public BeaconOfUnrest() {
        addEffect(EffectSlot.SPELL, new ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect());
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
