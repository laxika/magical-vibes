package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSameNameAsDyingCreatureToHandEffect;

@CardRegistration(set = "USG", collectorNumber = "34")
public class Remembrance extends Card {

    public Remembrance() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new MayEffect(new SearchLibraryForSameNameAsDyingCreatureToHandEffect(),
                        "Search for a card with the same name?"));
    }
}
