package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;

import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "48")
public class TrinketMage extends Card {

    public TrinketMage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryForCardTypesToHandEffect(Set.of(CardType.ARTIFACT), 1),
                        "Search your library for an artifact card with mana value 1 or less?"));
    }
}
