package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;

@CardRegistration(set = "ORI", collectorNumber = "37")
public class TotemGuideHartebeest extends Card {

    public TotemGuideHartebeest() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(new CardIsAuraPredicate()),
                        "Search your library for an Aura card?"));
    }
}
