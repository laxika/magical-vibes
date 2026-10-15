package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "128")
public class GrowthChamberGuardian extends Card {

    public GrowthChamberGuardian() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new AdaptEffect(2)),
                "{2}{G}: Adapt 2."
        ));

        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                new MayEffect(
                        new SearchLibraryEffect(new CardNamedPredicate("Growth-Chamber Guardian")),
                        "Search your library for a card named Growth-Chamber Guardian?"));
    }
}
