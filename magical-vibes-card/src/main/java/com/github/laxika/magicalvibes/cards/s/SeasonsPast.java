package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneCardOfEachManaValueFromGraveyardToHandEffect;

@CardRegistration(set = "SOI", collectorNumber = "226")
public class SeasonsPast extends Card {

    public SeasonsPast() {
        addEffect(EffectSlot.SPELL, new ReturnUpToOneCardOfEachManaValueFromGraveyardToHandEffect());
        addEffect(EffectSlot.SPELL, new PutSelfOnBottomOfOwnersLibraryEffect());
    }
}
