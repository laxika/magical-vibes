package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M10", collectorNumber = "52")
@CardRegistration(set = "MRD", collectorNumber = "35")
@CardRegistration(set = "HOP", collectorNumber = "9")
@CardRegistration(set = "SLD", collectorNumber = "332")
@CardRegistration(set = "SLD", collectorNumber = "703")
@CardRegistration(set = "SLD", collectorNumber = "1084")
@CardRegistration(set = "SLD", collectorNumber = "2090")
public class Fabricate extends Card {

    public Fabricate() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardTypePredicate(CardType.ARTIFACT)));
    }
}
