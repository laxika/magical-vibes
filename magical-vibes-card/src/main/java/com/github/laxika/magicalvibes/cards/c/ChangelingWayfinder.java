package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;

public class ChangelingWayfinder extends Card {

    public ChangelingWayfinder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new SearchLibraryForBasicLandToHandEffect(), "Search your library for a basic land card?"));
    }
}
