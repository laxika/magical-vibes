package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentYouControlAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "19")
public class YannikScavengingSentinel extends Card {

    private static final String PARTNER_NAME = "Nikara, Lair Scavenger";

    public YannikScavengingSentinel() {
        var partnerWith = new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put " + PARTNER_NAME + " into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER);
        var partnerTarget = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player."));
        registerEffectTargetIndex(partnerWith, partnerTarget.getIndex());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, partnerWith);

        var anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        var distributeCounters =
                DistributeCountersAmongTargetsEffect.chosenAmongAnyNumberOfTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue(), null);
        var counterTargets = target(TargetFilters.creature(), 0, 99);
        registerEffectTargetIndex(distributeCounters, counterTargets.getIndex());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExilePermanentYouControlAndTrackWithSourceEffect(anotherCreature, distributeCounters));
    }
}
