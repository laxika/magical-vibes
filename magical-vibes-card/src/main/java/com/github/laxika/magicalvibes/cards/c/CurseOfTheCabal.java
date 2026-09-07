package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnSuspendedCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "99")
public class CurseOfTheCabal extends Card {

    public CurseOfTheCabal() {
        PermanentTruePredicate allPermanents = new PermanentTruePredicate();
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                        new Divided(new PermanentCount(allPermanents, CountScope.TARGET_PLAYER), 2),
                        allPermanents,
                        SacrificeRecipient.TARGET_PLAYER));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(),
                "Suspend 2—{2}{B}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(2));

        addEffect(EffectSlot.SUSPENDED_EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                new SourceCardSuspended(),
                new MayEffect(
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(1, allPermanents, SacrificeRecipient.ACTIVE_PLAYER)
                                        .withRecordedSacrificeCount(),
                                new PutTimeCountersOnSuspendedCardEffect(2)),
                        "Sacrifice a permanent?",
                        null,
                        MayChoicePlayer.ACTIVE_PLAYER)));
    }
}
