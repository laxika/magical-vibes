package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardTypesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "192")
public class OmnivorousFlytrap extends Card {

    public OmnivorousFlytrap() {
        var deliriumAbility = new ConditionalEffect(
                new Delirium(),
                SequenceEffect.of(
                        DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                                CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new ConditionalEffect(
                                new CardTypesAmongCardsInGraveyardAtLeast(6),
                                new DoublePlusOneCountersOnTargetCreatureEffect())));

        target(TargetFilters.creature(), 1, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, deliriumAbility)
                .addEffect(EffectSlot.ON_ATTACK, deliriumAbility);
    }
}
