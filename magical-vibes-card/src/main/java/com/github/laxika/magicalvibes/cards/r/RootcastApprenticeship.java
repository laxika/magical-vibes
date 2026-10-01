package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "32")
@CardRegistration(set = "BLC", collectorNumber = "65")
public class RootcastApprenticeship extends Card {

    public RootcastApprenticeship() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");
        var opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent.");
        var tokenYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentIsTokenPredicate(), "Target must be a token you control.");
        var nontokenArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        var squirrel = new CreateTokenEffect(
                1, "Squirrel", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of());

        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Put two +1/+1 counters on target creature",
                        () -> new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        TargetFilters.creature()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Create a token that's a copy of target token you control",
                        CreateTokenCopyOfTargetPermanentEffect::new,
                        tokenYouControl),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Target player creates a 1/1 green Squirrel creature token",
                        () -> new CreateTokenForTargetPlayerEffect(squirrel),
                        anyPlayer),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Target opponent sacrifices a nontoken artifact of their choice",
                        () -> new SacrificePermanentsEffect(1, nontokenArtifact,
                                SacrificeRecipient.TARGET_PLAYER),
                        opponent)
        ), 3));
    }
}
