package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPlayCardsFromTopOfLibraryForLifeUntilEndOfTurnEffect;

@CardRegistration(set = "SPM", collectorNumber = "56")
public class GwenomRemorseless extends Card {

    public GwenomRemorseless() {
        addEffect(EffectSlot.ON_ATTACK, new MayPlayCardsFromTopOfLibraryForLifeUntilEndOfTurnEffect());
    }
}
