package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesGraveyardIntoLibraryEffect;

@CardRegistration(set = "RAV", collectorNumber = "59")
public class MnemonicNexus extends Card {

    public MnemonicNexus() {
        addSpellEffect(new EachPlayerShufflesGraveyardIntoLibraryEffect());
    }
}
