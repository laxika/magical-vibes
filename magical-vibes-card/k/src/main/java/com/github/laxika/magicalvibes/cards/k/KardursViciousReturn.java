package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "KHM", collectorNumber = "217")
public class KardursViciousReturn extends Card {

    public KardursViciousReturn() {
        addTrigger(Triggers.chapter(1), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                DealDamageEffect.toAnyTarget(3)))),
                "Sacrifice a creature?"));

        addTrigger(Triggers.chapter(2), new DiscardEffect(1, Subjects.eachPlayer()));

        addTrigger(Triggers.chapter(3), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .plusOneCounterCount(1)
                .grantHasteUntilNextTurn(true)
                .build());
    }
}
