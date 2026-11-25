package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "672")
public class VillainousSyndication extends Card {

    public VillainousSyndication() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.VILLAIN)),
                        new MillEffect(1, Subjects.you()),
                        PutCountersEffect.onSelf(CounterType.PLAN)
                ),
                "Tap an untapped Villain you control: Mill a card. Put a plan counter on this enchantment. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        // "When the fourth plan counter is put on this enchantment" is qualified by the counter placement
        // itself, so the threshold is checked only when the counter is put, not again on resolution.
        addTrigger(Triggers.countersPutOnThis(), TriggerOptions.DEFAULT.withEventCondition(
                new SourceCounterThreshold(4, CounterType.PLAN)),
                SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build()))));
    }
}
