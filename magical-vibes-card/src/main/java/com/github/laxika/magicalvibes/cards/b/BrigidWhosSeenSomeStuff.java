package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "274")
@CardRegistration(set = "MB2", collectorNumber = "510")
public class BrigidWhosSeenSomeStuff extends Card {

    public BrigidWhosSeenSomeStuff() {
        // Thoughtweft shares Brigid's printed keyword abilities with your Kithkin.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.VIGILANCE, Keyword.NIMBLE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.KITHKIN)));
    }
}
