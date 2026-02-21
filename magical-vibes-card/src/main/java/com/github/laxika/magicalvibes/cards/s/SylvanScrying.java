package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "302")
public class SylvanScrying extends Card {

    public SylvanScrying() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCardTypesToHandEffect(Set.of(CardType.LAND)));
    }
}
