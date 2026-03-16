package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M10", collectorNumber = "52")
public class Fabricate extends Card {

    public Fabricate() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCardTypesToHandEffect(
                new CardTypePredicate(CardType.ARTIFACT)));
    }
}
