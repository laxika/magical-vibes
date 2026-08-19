package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameToHandEffect;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "77")
public class MastermindsAcquisition extends Card {

    public MastermindsAcquisition() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a card, put it into your hand, then shuffle",
                        new SearchLibraryEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a card you own from outside the game into your hand",
                        new SearchOutsideGameToHandEffect())
        )));
    }
}
