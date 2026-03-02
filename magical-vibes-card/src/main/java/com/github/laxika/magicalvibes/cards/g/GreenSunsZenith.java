package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "81")
public class GreenSunsZenith extends Card {

    public GreenSunsZenith() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect(CardColor.GREEN));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
