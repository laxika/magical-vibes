package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "BNG", collectorNumber = "140")
public class Skyreaping extends Card {

    public Skyreaping() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN),
                false, false, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
