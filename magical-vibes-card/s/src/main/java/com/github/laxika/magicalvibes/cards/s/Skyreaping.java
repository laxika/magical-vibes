package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "BNG", collectorNumber = "140")
@CardRegistration(set = "PIO", collectorNumber = "349")
public class Skyreaping extends Card {

    public Skyreaping() {
        addSpellEffect(DealDamageEffect.toEachCreature(new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN),
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
