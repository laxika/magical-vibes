package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;

import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "34")
public class TreasureMage extends Card {

    public TreasureMage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryForCardTypesToHandEffect(Set.of(CardType.ARTIFACT), 6, Integer.MAX_VALUE),
                        "Search your library for an artifact card with mana value 6 or greater?"));
    }
}
