package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.CastDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "NCC", collectorNumber = "77")
@CardRegistration(set = "NCC", collectorNumber = "177")
public class OskarRubbishReclaimer extends Card {

    public OskarRubbishReclaimer() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new DistinctManaValuesAmongCardsInGraveyard()));
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new CastDiscardedCardFromGraveyardEffect());
    }
}
