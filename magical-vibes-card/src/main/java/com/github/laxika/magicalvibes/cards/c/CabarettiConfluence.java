package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "69")
@CardRegistration(set = "NCC", collectorNumber = "169")
public class CabarettiConfluence extends Card {

    public CabarettiConfluence() {
        setAllowSharedTargets(true);

        var artifactOrEnchantmentPredicate = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()));
        var artifactOrEnchantment = new PermanentPredicateTargetFilter(
                artifactOrEnchantmentPredicate,
                "Target must be an artifact or enchantment.");
        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Create a token that's a copy of target creature you control. It gains haste. "
                                + "Sacrifice it at the beginning of the next end step.",
                        () -> new CreateTokenCopyOfTargetPermanentEffect(true, false, true),
                        TargetFilters.creatureYouControl()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Exile target artifact or enchantment",
                        () -> new ExileTargetPermanentEffect(artifactOrEnchantmentPredicate),
                        artifactOrEnchantment),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Creatures target player controls get +1/+1 and gain first strike until end of turn",
                        () -> SequenceEffect.of(
                                new BoostAllCreaturesEffect(1, 1, EachPermanentScope.TARGET_PLAYER),
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET_PLAYERS_CREATURES)),
                        anyPlayer)
        ), 3));
    }
}
