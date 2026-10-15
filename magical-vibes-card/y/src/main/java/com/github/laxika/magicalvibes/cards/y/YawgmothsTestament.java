package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFaceUpCardsFromExileThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurnEffect;

@CardRegistration(set = "MB1", collectorNumber = "52")
public class YawgmothsTestament extends Card {

    public YawgmothsTestament() {
        addEffect(EffectSlot.SPELL, new AllowPlayFaceUpCardsFromExileThisTurnEffect());
        addEffect(EffectSlot.SPELL, new PutCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurnEffect());
    }
}
