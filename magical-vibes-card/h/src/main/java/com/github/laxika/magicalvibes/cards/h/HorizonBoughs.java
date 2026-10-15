package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsUntapDuringEachPlayersUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "OPCA", collectorNumber = "37")
public class HorizonBoughs extends Card {

    public HorizonBoughs() {
        addEffect(EffectSlot.STATIC, new AllPermanentsUntapDuringEachPlayersUntapStepEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new MayEffect(
                new SearchLibraryEffect(new Fixed(3), CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                "Search your library for up to three basic land cards?"));
    }
}
