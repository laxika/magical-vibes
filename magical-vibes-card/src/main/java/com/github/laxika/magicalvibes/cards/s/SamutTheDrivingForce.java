package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerSpeed;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DFT", collectorNumber = "222")
public class SamutTheDrivingForce extends Card {

    public SamutTheDrivingForce() {
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new ControllerSpeed(),
                new Fixed(0),
                GrantScope.OWN_CREATURES
        ));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new ControllerSpeed(),
                CostModificationScope.SELF
        ));
    }
}
