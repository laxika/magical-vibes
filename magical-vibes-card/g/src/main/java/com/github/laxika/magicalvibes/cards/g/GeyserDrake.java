package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "51")
public class GeyserDrake extends Card {

    public GeyserDrake() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotControllerTurn(),
                new ReduceCastCostForMatchingSpellsEffect(
                        new CardTruePredicate(), 1, CostModificationScope.SELF)));
    }
}
