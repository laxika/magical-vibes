package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "HOC", collectorNumber = "40")
@CardRegistration(set = "HOC", collectorNumber = "80")
public class AndRilNarsilReforged extends Card {

    public AndRilNarsilReforged() {
        addEffect(EffectSlot.STATIC, new AscendEffect());

        PutCounterOnEachControlledPermanentEffect oneCounter =
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate());
        PutCounterOnEachControlledPermanentEffect twoCounters =
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2, new PermanentIsCreaturePredicate());
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.unless(new ControllerHasCityBlessing(),
                twoCounters));
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.unless(
                new NotCondition(new ControllerHasCityBlessing()), oneCounter));

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
