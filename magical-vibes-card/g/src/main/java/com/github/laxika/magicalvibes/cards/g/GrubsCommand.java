package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndReturnMilledCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "228")
@CardRegistration(set = "ECL", collectorNumber = "340")
public class GrubsCommand extends Card {

    public GrubsCommand() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var goblinYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                "Target must be a Goblin you control.");
        var artifactOrCreature = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())),
                "Target must be an artifact or creature.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target Goblin you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        goblinYouControl),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures target player controls get +1/+1 and gain haste until end of turn",
                        List.of(
                                new BoostAllCreaturesEffect(1, 1, EachPermanentScope.TARGET_PLAYER),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET_PLAYERS_CREATURES)),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact or creature",
                        new DestroyTargetPermanentEffect(),
                        artifactOrCreature),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player mills five cards, then puts each Goblin card milled this way into their hand",
                        new MillTargetPlayerAndReturnMilledCardsToHandEffect(
                                5, new CardSubtypePredicate(CardSubtype.GOBLIN)),
                        anyPlayer)
        ), 2));
    }
}
