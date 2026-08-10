package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "189")
public class JinxedChoker extends Card {

    public JinxedChoker() {
        setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new TargetPlayerGainsControlOfSourceCreatureEffect(),
                new PutCountersOnSelfEffect(CounterType.CHARGE)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(new CountersOnSource(CounterType.CHARGE), DamageRecipient.CONTROLLER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a charge counter on Jinxed Choker",
                                new PutCountersOnSelfEffect(CounterType.CHARGE)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Remove a charge counter from Jinxed Choker",
                                new RemoveCounterFromSourceEffect(CounterType.CHARGE, 1))))),
                "{3}: Put a charge counter on Jinxed Choker or remove one from it."
        ));
    }
}
