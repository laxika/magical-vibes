package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "107")
public class GargantuanLeech extends Card {

    public GargantuanLeech() {
        // This spell costs {1} less to cast for each Cave you control and each Cave card in your graveyard.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new Sum(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.CAVE), CountScope.CONTROLLER),
                new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.CAVE), CountScope.CONTROLLER))));
    }
}
