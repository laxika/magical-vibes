package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

public class BeaconOfUnrest extends Card {

    public BeaconOfUnrest() {
        super("Beacon of Unrest", CardType.SORCERY, "{3}{B}{B}", CardColor.BLACK);

        setCardText("Put target artifact or creature card from a graveyard onto the battlefield under your control.\nShuffle Beacon of Unrest into its owner's library.");
        addEffect(EffectSlot.SPELL, new ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect());
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
