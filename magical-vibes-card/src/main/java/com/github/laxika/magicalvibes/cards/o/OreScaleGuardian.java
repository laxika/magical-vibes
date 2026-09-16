package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MH1", collectorNumber = "137")
public class OreScaleGuardian extends Card {

    public OreScaleGuardian() {
        // This spell costs {1} less to cast for each land card in your graveyard.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER)));
    }
}
