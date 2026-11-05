package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.CardTypesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "DSK", collectorNumber = "192")
public class OmnivorousFlytrap extends Card {

    public OmnivorousFlytrap() {
        TriggerOptions delirium = TriggerOptions.DEFAULT.withInterveningIf(new Delirium());
        var deliriumAbility = SequenceEffect.of(
                DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                        CounterType.PLUS_ONE_PLUS_ONE, 2),
                new ConditionalEffect(
                        new CardTypesAmongCardsInGraveyardAtLeast(6),
                        new DoublePlusOneCountersOnTargetCreatureEffect()));

        target(TargetFilters.creature(), 1, 2)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), delirium, deliriumAbility)
                .addTrigger(Triggers.attacks(), delirium, deliriumAbility);
    }
}
