package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;

@CardRegistration(set = "SCG", collectorNumber = "16")
public class GildedLight extends Card {

    public GildedLight() {
        addEffect(EffectSlot.SPELL, new GrantControllerKeywordUntilEndOfTurnEffect(Keyword.SHROUD));
        addCycling("{2}");
    }
}
