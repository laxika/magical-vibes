package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersToCreaturesAndVehiclesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "195")
public class CaradoraHeartOfAlacria extends Card {

    public CaradoraHeartOfAlacria() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.MOUNT),
                                new CardSubtypePredicate(CardSubtype.VEHICLE)))),
                        "Search your library for a Mount or Vehicle card?"));
        addEffect(EffectSlot.STATIC, new AddOnePlusOneCountersToCreaturesAndVehiclesEffect());
    }
}
