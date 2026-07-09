package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ActivationCount;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.BlockedByMinCreatures;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerPoisoned;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.DidntGainLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardNameMatchesEnteringPermanent;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryColor;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.WonClash;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * The single evaluation point for every {@link Condition} in the engine.
 *
 * <p>The switch in {@link #isMet} is exhaustive over the sealed {@link Condition} hierarchy —
 * adding a condition without an evaluation is a compile error, never a silent
 * {@code false}. All evaluation contexts (stack resolution, trigger collection, ETB gating,
 * combat triggers, static bonus computation) call this service with a {@link ConditionContext}
 * describing where the values (kicked flag, source zone, x value, …) come from at that site.</p>
 */
@Service
@RequiredArgsConstructor
public class ConditionEvaluationService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final StaticEffectSupport staticEffectSupport;

    /**
     * Evaluates whether the given condition is currently met.
     */
    public boolean isMet(GameData gameData, Condition condition, ConditionContext ctx) {
        return switch (condition) {
            case Metalcraft ignored ->
                    isMetalcraftMet(gameData, ctx);
            case Morbid ignored ->
                    gameQueryService.isMorbidMet(gameData);
            case CreatureDiedUnderYourControlThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.creatureDeathCountThisTurn.getOrDefault(ctx.controllerId(), 0) > 0;
            case Kicked ignored ->
                    ctx.kicked();
            case NotKicked ignored ->
                    !ctx.kicked();
            case Raid ignored ->
                    ctx.controllerId() != null
                            && gameData.playersDeclaredAttackersThisTurn.contains(ctx.controllerId());
            case Equipped ignored ->
                    isSourceEquipped(gameData, ctx);
            case GainedLifeThisTurn ignored ->
                    ctx.controllerId() != null && gameData.hasGainedLifeThisTurn(ctx.controllerId());
            case DidntGainLifeThisTurn ignored ->
                    ctx.controllerId() != null && !gameData.hasGainedLifeThisTurn(ctx.controllerId());
            case ControlsPermanent c ->
                    controlsMatchingPermanent(gameData, ctx, c.filter());
            case ControlsAnotherPermanent c ->
                    controlsAnotherMatchingPermanent(gameData, ctx, c.filter());
            case OpponentControlsPermanent c ->
                    opponentControlsMatchingPermanent(gameData, ctx, c.filter());
            case AnyPlayerControlsPermanent c ->
                    anyPlayerControlsMatchingPermanent(gameData, ctx, c.filter());
            case AnyPlayerControlsPermanentCount c ->
                    countMatchingPermanentsOnBattlefield(gameData, ctx, c.filter()) >= c.minCount();
            case ControlsPermanentCount c ->
                    countControlledMatchingPermanents(gameData, ctx, c.filter()) >= c.minCount();
            case ControlsPermanentCountAtMost c ->
                    countControlledMatchingPermanents(gameData, ctx, c.filter()) <= c.maxCount();
            case NoOtherPermanent c ->
                    noOtherMatchingPermanent(gameData, ctx, c.filter());
            case ControllerLifeAtLeast c ->
                    ctx.controllerId() != null
                            && gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 20) >= c.threshold();
            case ControllerLifeAtMost c ->
                    ctx.controllerId() != null
                            && gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 20) <= c.threshold();
            case GraveyardCardThreshold c ->
                    countMatchingGraveyardCards(gameData, ctx, c) >= c.threshold();
            case CastFromZone c ->
                    c.sourceZone() == ctx.sourceZone();
            case CastNotFromHand ignored ->
                    ctx.sourceZone() != Zone.HAND;
            case DidntAttack ignored ->
                    sourceDidntAttackThisTurn(gameData, ctx);
            case AttacksAlone ignored ->
                    countAttackingCreatures(gameData, ctx.controllerId()) == 1;
            case MinimumAttackers c ->
                    ctx.xValue() >= c.minimumAttackers();
            case HasAttacker c ->
                    hasMatchingAttacker(gameData, ctx, c.predicate());
            case NoSpellsCastLastTurn ignored ->
                    noSpellsCastLastTurn(gameData);
            case TwoOrMoreSpellsCastLastTurn ignored ->
                    gameData.spellsCastLastTurn.values().stream().anyMatch(count -> count >= 2);
            case DefendingPlayerControlsPermanent c ->
                    defendingPlayerControlsMatchingPermanent(gameData, ctx, c.filter());
            case DefendingPlayerPoisoned ignored ->
                    isDefendingPlayerPoisoned(gameData, ctx.controllerId());
            case OpponentPoisoned ignored ->
                    isAnyOpponentPoisoned(gameData, ctx.controllerId());
            case OpponentDealtDamageThisTurn ignored ->
                    wasAnyOpponentDealtDamageThisTurn(gameData, ctx.controllerId());
            case ActivationCount c ->
                    activationCountThisTurn(gameData, ctx, c.abilityIndex()) >= c.threshold();
            case PermanentEnteredThisTurn c ->
                    countPermanentsEnteredThisTurn(gameData, ctx, c) >= c.minCount();
            case ControllerCastAnotherSpellThisTurn c ->
                    ctx.controllerId() != null && gameQueryService.hasControllerCastAnotherSpellThisTurn(
                            gameData, ctx.controllerId(), ctx.sourceCard(), c.filter());
            case SpellManaSpentAtLeast c ->
                    ctx.xValue() >= c.minMana();
            case ControllerTurn ignored ->
                    ctx.controllerId() != null && ctx.controllerId().equals(gameData.activePlayerId);
            case NotControllerTurn ignored ->
                    ctx.controllerId() != null && !ctx.controllerId().equals(gameData.activePlayerId);
            case TargetPermanentMatches c -> {
                Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
                yield target != null && predicateEvaluationService.matchesPermanentPredicate(gameData, target, c.filter());
            }
            case SourceHasSubtype c ->
                    sourceHasSubtype(gameData, ctx, c.subtype());
            case SelfHasKeyword c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.hasKeyword(c.keyword());
            }
            case SourceCounterThreshold c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.getCounterCount(c.counterType()) >= c.threshold();
            }
            case TopCardOfLibraryColor c ->
                    isTopCardOfLibraryColor(gameData, ctx.controllerId(), c);
            case BlockedByMinCreatures c ->
                    countBlockersOfSource(gameData, ctx) >= c.minBlockers();
            case ImprintedCardNameMatchesEnteringPermanent ignored ->
                    imprintedCardNameMatches(gameData, ctx);
            case OpponentControlsMoreCreatures c ->
                    anyOpponentControlsAtLeastNMoreCreatures(gameData, ctx.controllerId(), c.minimumCreatureDifference());
            case CardsLeftGraveyardThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoseCardsLeftGraveyardThisTurn.contains(ctx.controllerId());
            case WonClash ignored ->
                    ctx.controllerId() != null
                            && gameData.lastClashWonByController.getOrDefault(ctx.controllerId(), false);
        };
    }

    /**
     * True if any opponent controls at least {@code minimumDifference} more creatures than the
     * controller (Avatar of Might's cast-cost reduction).
     */
    private boolean anyOpponentControlsAtLeastNMoreCreatures(GameData gameData, UUID controllerId, int minimumDifference) {
        if (controllerId == null) return false;
        int yourCreatures = countCreaturesControlled(gameData, controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (countCreaturesControlled(gameData, candidateOpponentId) >= yourCreatures + minimumDifference) {
                return true;
            }
        }
        return false;
    }

    private int countCreaturesControlled(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Resolves the source permanent from the context, preferring the permanent handed in by
     * the call site and falling back to a battlefield lookup by id.
     */
    private Permanent sourcePermanent(GameData gameData, ConditionContext ctx) {
        if (ctx.sourcePermanent() != null) return ctx.sourcePermanent();
        if (ctx.sourcePermanentId() == null) return null;
        return gameQueryService.findPermanentById(gameData, ctx.sourcePermanentId());
    }

    /**
     * Matches a permanent against a predicate. Static bonus computation must use the
     * recursion-safe static filter matcher (the general matcher can re-enter static bonus
     * computation via e.g. creature checks); every other context uses the general matcher.
     */
    private boolean matchesPermanent(GameData gameData, Permanent permanent, PermanentPredicate filter,
                                     ConditionContext ctx) {
        return ctx.staticEvaluation()
                ? staticEffectSupport.matchesStaticFilter(permanent, filter)
                : predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter);
    }

    /** Returns {@code true} if the given permanent is the condition's own source. */
    private boolean isSource(Permanent permanent, ConditionContext ctx) {
        return (ctx.sourcePermanentId() != null && permanent.getId().equals(ctx.sourcePermanentId()))
                || (ctx.sourceCard() != null && permanent.getCard() == ctx.sourceCard());
    }

    /**
     * Metalcraft: three or more controlled artifacts. Static bonus computation must count
     * artifacts without consulting static card-type grants (the grant lookup re-enters static
     * bonus computation); every other context uses the general artifact check.
     */
    private boolean isMetalcraftMet(GameData gameData, ConditionContext ctx) {
        // No controller (e.g. static self bonus computed while the permanent is being removed):
        // controller-dependent conditions are simply not met
        if (ctx.controllerId() == null) return false;
        if (!ctx.staticEvaluation()) {
            return gameQueryService.isMetalcraftMet(gameData, ctx.controllerId());
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream().filter(gameQueryService::isArtifact).count() >= 3;
    }

    private boolean isSourceEquipped(GameData gameData, ConditionContext ctx) {
        UUID sourcePermanentId = ctx.sourcePermanentId();
        if (sourcePermanentId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && sourcePermanentId.equals(perm.getAttachedTo())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean controlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx));
    }

    private boolean controlsAnotherMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream()
                .anyMatch(p -> !isSource(p, ctx) && matchesPermanent(gameData, p, filter, ctx));
    }

    private boolean opponentControlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            if (battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx))) {
                return true;
            }
        }
        return false;
    }

    private boolean anyPlayerControlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            if (battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx))) {
                return true;
            }
        }
        return false;
    }

    private long countMatchingPermanentsOnBattlefield(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        long count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            count += battlefield.stream().filter(p -> matchesPermanent(gameData, p, filter, ctx)).count();
        }
        return count;
    }

    private boolean defendingPlayerControlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return false;
        UUID defendingPlayerId = gameQueryService.getOpponentId(gameData, ctx.controllerId());
        if (defendingPlayerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(defendingPlayerId);
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx));
    }

    private long countControlledMatchingPermanents(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        return battlefield.stream().filter(p -> matchesPermanent(gameData, p, filter, ctx)).count();
    }

    private boolean noOtherMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return true;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return true;
        return battlefield.stream()
                .noneMatch(p -> !isSource(p, ctx) && matchesPermanent(gameData, p, filter, ctx));
    }

    private int countMatchingGraveyardCards(GameData gameData, ConditionContext ctx, GraveyardCardThreshold c) {
        if (ctx.controllerId() == null) return 0;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        if (graveyard == null) return 0;
        int count = 0;
        for (Card card : graveyard) {
            if (card.isToken()) continue;
            boolean matches = ctx.staticEvaluation()
                    ? predicateEvaluationService.matchesCardPredicate(card, c.filter(), null)
                    : c.filter() == null || predicateEvaluationService.matchesCardPredicate(card, c.filter(),
                            null, gameData, ctx.controllerId());
            if (matches) count++;
        }
        return count;
    }

    private boolean sourceDidntAttackThisTurn(GameData gameData, ConditionContext ctx) {
        if (ctx.sourcePermanentId() == null) return true;
        Permanent source = sourcePermanent(gameData, ctx);
        if (source == null) return false;
        return !source.isAttackedThisTurn();
    }

    private long countAttackingCreatures(GameData gameData, UUID controllerId) {
        if (controllerId == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;
        return battlefield.stream().filter(Permanent::isAttacking).count();
    }

    private boolean hasMatchingAttacker(GameData gameData, ConditionContext ctx, PermanentPredicate predicate) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream()
                .filter(Permanent::isAttacking)
                .anyMatch(p -> matchesPermanent(gameData, p, predicate, ctx));
    }

    private boolean noSpellsCastLastTurn(GameData gameData) {
        if (gameData.spellsCastLastTurn.isEmpty()) return true;
        return gameData.spellsCastLastTurn.values().stream().mapToInt(Integer::intValue).sum() == 0;
    }

    private boolean isDefendingPlayerPoisoned(GameData gameData, UUID attackingPlayerId) {
        if (attackingPlayerId == null) return false;
        UUID defendingPlayerId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        if (defendingPlayerId == null) return false;
        return gameData.playerPoisonCounters.getOrDefault(defendingPlayerId, 0) > 0;
    }

    private boolean isAnyOpponentPoisoned(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)
                    && gameData.playerPoisonCounters.getOrDefault(playerId, 0) > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean wasAnyOpponentDealtDamageThisTurn(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId) && gameData.playersDealtDamageThisTurn.contains(playerId)) {
                return true;
            }
        }
        return false;
    }

    private int activationCountThisTurn(GameData gameData, ConditionContext ctx, int abilityIndex) {
        if (ctx.sourcePermanentId() == null) return 0;
        var perAbilityCounts = gameData.activatedAbilityUsesThisTurn.get(ctx.sourcePermanentId());
        if (perAbilityCounts == null) return 0;
        return perAbilityCounts.getOrDefault(abilityIndex, 0);
    }

    private long countPermanentsEnteredThisTurn(GameData gameData, ConditionContext ctx, PermanentEnteredThisTurn c) {
        if (ctx.controllerId() == null) return 0;
        List<Card> entered = gameData.permanentsEnteredBattlefieldThisTurn
                .getOrDefault(ctx.controllerId(), List.of());
        return entered.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, c.predicate(), null))
                .count();
    }

    private boolean sourceHasSubtype(GameData gameData, ConditionContext ctx, CardSubtype subtype) {
        Permanent source = sourcePermanent(gameData, ctx);
        if (source != null) {
            return source.getCard().getSubtypes().contains(subtype);
        }
        return ctx.sourceCard() != null && ctx.sourceCard().getSubtypes().contains(subtype);
    }

    private boolean isTopCardOfLibraryColor(GameData gameData, UUID controllerId, TopCardOfLibraryColor c) {
        if (controllerId == null) return false;
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) return false;
        return deck.getFirst().getColors().contains(c.color());
    }

    private int countBlockersOfSource(GameData gameData, ConditionContext ctx) {
        if (ctx.sourcePermanentId() == null) return 0;
        final int[] blockerCount = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(ctx.sourcePermanentId())) {
                blockerCount[0]++;
            }
        });
        return blockerCount[0];
    }

    private boolean imprintedCardNameMatches(GameData gameData, ConditionContext ctx) {
        if (ctx.triggeringCard() == null) return false;
        Permanent source = sourcePermanent(gameData, ctx);
        if (source == null) return false;
        Card imprintedCard = source.getCard().getImprintedCard();
        return imprintedCard != null && imprintedCard.getName().equals(ctx.triggeringCard().getName());
    }
}
