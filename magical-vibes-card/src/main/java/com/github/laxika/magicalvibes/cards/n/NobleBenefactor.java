package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForCardsToHandEffect;

@CardRegistration(set = "WTH", collectorNumber = "44")
public class NobleBenefactor extends Card {

    public NobleBenefactor() {
        addEffect(EffectSlot.ON_DEATH, EachPlayerMaySearchLibraryForCardsToHandEffect.oneCard());
    }
}
