package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.ActivationCount;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHandEmpty;
import com.github.laxika.magicalvibes.model.condition.AnyLibraryAtMost;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.BlockedByMinCreatures;
import com.github.laxika.magicalvibes.model.condition.CameUnderControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.condition.ChosenColorStrictlyMostCommonAmongOpponentNontokens;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreLifeThanAnOpponent;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.condition.CreatureAttackingController;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerPoisoned;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.condition.DevouredCreature;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.DidntGainLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.condition.EndStepPlayerDidntCastCreatureSpell;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardNameMatchesEnteringPermanent;
import com.github.laxika.magicalvibes.model.condition.CastForProwlCost;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.condition.NoPlayerHasCardsInHand;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageToOpponentThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceDamagedCreatureDiedThisTurn;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.condition.SourceCanSoulbond;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryColor;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.WonClash;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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

    private static final PermanentIsCreaturePredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final StaticEffectSupport staticEffectSupport;

    /**
     * Evaluates whether the given condition is currently met.
     */
    public boolean isMet(GameData gameData, Condition condition, ConditionContext ctx) {
        return switch (condition) {
            case NotCondition c ->
                    !isMet(gameData, c.inner(), ctx);
            case AllConditions c ->
                    c.conditions().stream().allMatch(inner -> isMet(gameData, inner, ctx));
            case CreatureAttackingController ignored ->
                    ctx.controllerId() != null && creatureAttackingPlayer(gameData, ctx.controllerId());
            case AllOf c ->
                    c.conditions().stream().allMatch(inner -> isMet(gameData, inner, ctx));
            case AnyOf c ->
                    c.conditions().stream().anyMatch(inner -> isMet(gameData, inner, ctx));
            case CameUnderControlThisTurn ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isSummoningSick();
            }
            case Metalcraft ignored ->
                    isMetalcraftMet(gameData, ctx);
            case Delirium ignored ->
                    isDeliriumMet(gameData, ctx);
            case Coven ignored ->
                    isCovenMet(gameData, ctx);
            case Morbid ignored ->
                    gameQueryService.isMorbidMet(gameData);
            case CreatureDiedUnderYourControlThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.creatureDeathCountThisTurn.getOrDefault(ctx.controllerId(), 0) > 0;
            case Kicked ignored ->
                    ctx.kicked();
            case NotKicked ignored ->
                    !ctx.kicked();
            case CastForProwlCost ignored ->
                    ctx.prowl();
            case Raid ignored ->
                    ctx.controllerId() != null
                            && gameData.playersDeclaredAttackersThisTurn.contains(ctx.controllerId());
            case AttackedWithCreaturesThisTurn c ->
                    ctx.controllerId() != null
                            && gameData.creaturesAttackedCountThisTurn.getOrDefault(ctx.controllerId(), 0) >= c.minimum();
            case Equipped ignored ->
                    isSourceEquipped(gameData, ctx);
            case Enchanted ignored ->
                    isSourceEnchanted(gameData, ctx);
            case EndStepPlayerDidntCastCreatureSpell ignored ->
                    ctx.targetId() != null
                            && gameData.getSpellsCastThisTurn(ctx.targetId()).stream()
                                    .noneMatch(spell -> spell.hasType(CardType.CREATURE));
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
            case AnyPlayerControlsPermanentCountAtMost c ->
                    countMatchingPermanentsOnBattlefield(gameData, ctx, c.filter()) <= c.maxCount();
            case ControlsPermanentCount c ->
                    countControlledMatchingPermanents(gameData, ctx, c.filter()) >= c.minCount();
            case ControlsPermanentCountAtMost c ->
                    countControlledMatchingPermanents(gameData, ctx, c.filter()) <= c.maxCount();
            case ControlsOtherPermanentCount c ->
                    countOtherControlledMatchingPermanents(gameData, ctx, c.filter()) >= c.minCount();
            case ControlledCreaturesTotalPowerAtLeast c ->
                    controlledCreaturesTotalPower(gameData, ctx) >= c.threshold();
            case NoOtherPermanent c ->
                    noOtherMatchingPermanent(gameData, ctx, c.filter());
            case ControllerHasMoreLifeThanAnOpponent ignored ->
                    controllerHasMoreLifeThanAnOpponent(gameData, ctx.controllerId());
            case ControllerLifeAtLeast c ->
                    ctx.controllerId() != null
                            && gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 20) >= c.threshold();
            case ControllerLifeAtMost c ->
                    ctx.controllerId() != null
                            && gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 20) <= c.threshold();
            case GraveyardCardThreshold c ->
                    countMatchingGraveyardCards(gameData, ctx, c) >= c.threshold();
            case CardsAboveSelfInGraveyard c ->
                    countCardsAboveSelfInGraveyard(gameData, ctx, c) >= c.threshold();
            case CardsInLibraryAtLeast c ->
                    countCardsInLibrary(gameData, ctx.controllerId()) >= c.threshold();
            case AnyLibraryAtMost c ->
                    anyLibraryAtMost(gameData, c.threshold());
            case CardsInHandAtLeast c ->
                    countCardsInHand(gameData, ctx.controllerId()) >= c.threshold();
            case CardsInHandAtMost c ->
                    countCardsInHand(gameData, ctx.controllerId()) <= c.threshold();
            case ActivePlayerHandEmpty ignored ->
                    countCardsInHand(gameData, gameData.activePlayerId) == 0;
            case ControllerHandEmpty ignored ->
                    countCardsInHand(gameData, ctx.controllerId()) == 0;
            case CastFromZone c ->
                    c.sourceZone() == ctx.sourceZone();
            case CastNotFromHand ignored ->
                    ctx.sourceZone() != Zone.HAND;
            case DidntAttack ignored ->
                    sourceDidntAttackThisTurn(gameData, ctx);
            case AttacksAlone ignored ->
                    countAttackingCreatures(gameData, ctx.controllerId()) == 1;
            case FirstCombatPhase ignored ->
                    gameData.combatPhasesThisTurn == 1;
            case MinimumAttackers c ->
                    ctx.xValue() >= c.minimumAttackers();
            case HasAttacker c ->
                    hasMatchingAttacker(gameData, ctx, c.predicate());
            case NoPlayerHasCardsInHand ignored ->
                    noPlayerHasCardsInHand(gameData);
            case AnOpponentHandEmpty ignored ->
                    isAnyOpponentHandEmpty(gameData, ctx.controllerId());
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
            case OpponentDealtDamageThisTurn c ->
                    wasAnyOpponentDealtDamageThisTurn(gameData, ctx.controllerId(), c.minimumAmount());
            case SelfDealtDamageToOpponentThisTurn ignored ->
                    sourceDealtDamageToOpponentThisTurn(gameData, ctx);
            case SourceDamagedCreatureDiedThisTurn ignored ->
                    ctx.sourcePermanentId() != null
                            && gameData.sourcesWhoseDamagedCreaturesDiedThisTurn.contains(ctx.sourcePermanentId());
            case OpponentLostLifeThisTurn c ->
                    didAnyOpponentLoseLifeThisTurn(gameData, ctx.controllerId(), c.minimumAmount());
            case ActivationCount c ->
                    activationCountThisTurn(gameData, ctx, c.abilityIndex()) >= c.threshold();
            // Exact equality: "if this is the Nth time this ability has resolved this turn"
            // fires on the exact n-th resolution and never on a later one.
            case NthAbilityResolutionThisTurn c ->
                    ctx.sourcePermanentId() != null
                            && gameData.permanentAbilityResolutionsThisTurn
                                    .getOrDefault(ctx.sourcePermanentId(), 0) == c.n();
            case PermanentEnteredThisTurn c ->
                    countPermanentsEnteredThisTurn(gameData, ctx, c) >= c.minCount();
            case ControllerCastAnotherSpellThisTurn c ->
                    ctx.controllerId() != null && gameQueryService.hasControllerCastAnotherSpellThisTurn(
                            gameData, ctx.controllerId(), ctx.sourceCard(), c.filter());
            case SpellManaSpentAtLeast c ->
                    ctx.xValue() >= c.minMana();
            case SpellXAtLeast c ->
                    ctx.xValue() >= c.minX();
            case ColorSpentToCast c ->
                    ctx.sourceCard() != null
                            && gameData.getSpellCastColorsSpent(ctx.sourceCard().getId()).contains(c.color());
            case ControllerTurn ignored ->
                    ctx.controllerId() != null && ctx.controllerId().equals(gameData.activePlayerId);
            case NotControllerTurn ignored ->
                    ctx.controllerId() != null && !ctx.controllerId().equals(gameData.activePlayerId);
            case TargetPermanentMatches c -> {
                Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
                yield target != null && predicateEvaluationService.matchesPermanentPredicate(gameData, target, c.filter());
            }
            case TargetSpellMatches c -> {
                com.github.laxika.magicalvibes.model.StackEntry targetSpell = ctx.targetId() == null ? null
                        : gameData.stack.stream()
                                .filter(se -> se.getCard().getId().equals(ctx.targetId()))
                                .findFirst().orElse(null);
                yield targetSpell != null
                        && predicateEvaluationService.matchesStackEntryPredicate(targetSpell, c.filter(), null);
            }
            case SourceHasSubtype c ->
                    sourceHasSubtype(gameData, ctx, c.subtype());
            case SelfHasKeyword c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.hasKeyword(c.keyword());
            }
            case SourceCardInCommandZone ignored ->
                    isSourceCardInCommandZone(gameData, ctx);
            case SourceIsPaired ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.getPairedWithId() != null;
            }
            case SourceCanSoulbond ignored ->
                    canSoulbond(gameData, ctx);
            case SourceCounterThreshold c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.getCounterCount(c.counterType()) >= c.threshold();
            }
            case DevouredCreature ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && !source.getDevouredCreatures().isEmpty();
            }
            case SourceUntapped ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && !source.isTapped();
            }
            case SourceIsAttacking ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isAttacking();
            }
            case SourceIsTapped ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isTapped();
            }
            case TopCardOfLibraryColor c ->
                    isTopCardOfLibraryColor(gameData, ctx.controllerId(), c);
            case BlockedByMinCreatures c ->
                    countBlockersOfSource(gameData, ctx) >= c.minBlockers();
            case ImprintedCardNameMatchesEnteringPermanent ignored ->
                    imprintedCardNameMatches(gameData, ctx);
            case OpponentControlsMoreCreatures c ->
                    anyOpponentControlsAtLeastNMoreCreatures(gameData, ctx, c.minimumCreatureDifference());
            case OpponentControlsMoreLands ignored ->
                    gameQueryService.anyOpponentControlsMoreLands(gameData, ctx.controllerId());
            case ChosenColorStrictlyMostCommonAmongOpponentNontokens ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null
                        && ChosenColorStrictlyMostCommonAmongOpponentNontokens.isStrictlyMostCommon(
                                gameData, source, ctx.controllerId());
            }
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
    private boolean anyOpponentControlsAtLeastNMoreCreatures(GameData gameData, ConditionContext ctx, int minimumDifference) {
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) return false;
        int yourCreatures = countCreaturesControlled(gameData, controllerId, ctx);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (countCreaturesControlled(gameData, candidateOpponentId, ctx) >= yourCreatures + minimumDifference) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if the controller has strictly more life than at least one opponent
     * (Feudkiller's Verdict).
     */
    private boolean controllerHasMoreLifeThanAnOpponent(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        int yourLife = gameData.getLife(controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (yourLife > gameData.getLife(candidateOpponentId)) {
                return true;
            }
        }
        return false;
    }

    /** Sum of the effective power of every creature the given player controls. */
    private int controlledCreaturesTotalPower(GameData gameData, ConditionContext ctx) {
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;
        int totalPower = 0;
        for (Permanent permanent : battlefield) {
            if (isCreatureForCondition(gameData, permanent, ctx)) {
                totalPower += gameQueryService.getEffectivePower(gameData, permanent);
            }
        }
        return totalPower;
    }

    private int countCreaturesControlled(GameData gameData, UUID playerId, ConditionContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (isCreatureForCondition(gameData, permanent, ctx)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Creature check for condition evaluation. During static bonus computation
     * ({@link ConditionContext#staticEvaluation()}) the fully layered
     * {@link GameQueryService#isCreature} would recurse back into static assembly, so the
     * recursion-safe static filter matcher is used instead — the same contract
     * {@link #matchesPermanent} follows for permanent predicates.
     */
    private boolean isCreatureForCondition(GameData gameData, Permanent permanent, ConditionContext ctx) {
        return ctx.staticEvaluation()
                ? staticEffectSupport.matchesStaticFilter(permanent, CREATURE_FILTER)
                : gameQueryService.isCreature(gameData, permanent);
    }

    /** True if any opponent controls strictly more lands than the controller (Gift of Estates). */
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
        if (ctx.staticEvaluation()) {
            return staticEffectSupport.matchesStaticFilter(permanent, filter);
        }
        // Pass source card/controller so ownership and "is source" predicates work in conditions
        // (e.g. Gisela's "own and control Gisela and Bruna" intervening-if).
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceControllerId(ctx.controllerId());
        if (ctx.sourceCard() != null) {
            filterContext = filterContext.withSourceCardId(ctx.sourceCard().getId());
        } else if (ctx.sourcePermanent() != null) {
            filterContext = filterContext.withSourceCardId(ctx.sourcePermanent().getOriginalCard().getId());
        }
        return predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext);
    }

    /** Returns {@code true} if the given permanent is the condition's own source. */
    private boolean isSource(Permanent permanent, ConditionContext ctx) {
        return (ctx.sourcePermanentId() != null && permanent.getId().equals(ctx.sourcePermanentId()))
                || (ctx.sourceCard() != null && permanent.getCard() == ctx.sourceCard());
    }

    /** True when the stack entry's source card object is still in its controller's command zone. */
    private boolean isSourceCardInCommandZone(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null || ctx.sourceCard() == null) return false;
        List<Card> commandZone = gameData.playerCommandZones.get(ctx.controllerId());
        return commandZone != null && commandZone.contains(ctx.sourceCard());
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

    /** Coven: three or more controlled creatures with different effective powers. */
    private boolean isCovenMet(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null) return false;
        return gameQueryService.isCovenMet(gameData, ctx.controllerId());
    }

    /**
     * Delirium: four or more distinct card types among non-token cards in the controller's
     * graveyard (mirrors {@code CardTypesAmongCardsInGraveyard} with CONTROLLER scope).
     */
    private boolean isDeliriumMet(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null) return false;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        if (graveyard == null || graveyard.isEmpty()) return false;
        Set<CardType> found = EnumSet.noneOf(CardType.class);
        for (Card card : graveyard) {
            if (card.isToken()) continue;
            if (card.getType() != null) {
                found.add(card.getType());
            }
            found.addAll(card.getAdditionalTypes());
        }
        return found.size() >= 4;
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

    private boolean isSourceEnchanted(GameData gameData, ConditionContext ctx) {
        UUID sourcePermanentId = ctx.sourcePermanentId();
        if (sourcePermanentId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getSubtypes().contains(CardSubtype.AURA)
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

    private long countOtherControlledMatchingPermanents(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        return battlefield.stream()
                .filter(p -> !isSource(p, ctx) && matchesPermanent(gameData, p, filter, ctx))
                .count();
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

    /**
     * Counts cards matching the condition's filter positioned above the source card in its
     * controller's (ordered) graveyard. The graveyard is a list where later indices are higher
     * on the pile; "above" therefore means a strictly greater index than the source card.
     */
    private int countCardsAboveSelfInGraveyard(GameData gameData, ConditionContext ctx, CardsAboveSelfInGraveyard c) {
        if (ctx.controllerId() == null || ctx.sourceCard() == null) return 0;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        if (graveyard == null) return 0;
        int selfIndex = -1;
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getId().equals(ctx.sourceCard().getId())) {
                selfIndex = i;
                break;
            }
        }
        if (selfIndex < 0) return 0;
        int count = 0;
        for (int i = selfIndex + 1; i < graveyard.size(); i++) {
            Card above = graveyard.get(i);
            if (above.isToken()) continue;
            if (c.filter() == null
                    || predicateEvaluationService.matchesCardPredicate(above, c.filter(), null, gameData, ctx.controllerId())) {
                count++;
            }
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

    /**
     * True if any creature is attacking {@code playerId} themselves (the attack target is the
     * player, not a planeswalker they control). Qasali Ambusher's "a creature is attacking you".
     */
    private boolean creatureAttackingPlayer(GameData gameData, UUID playerId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (perm.isAttacking() && playerId.equals(perm.getAttackTarget())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasMatchingAttacker(GameData gameData, ConditionContext ctx, PermanentPredicate predicate) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream()
                .filter(Permanent::isAttacking)
                .anyMatch(p -> matchesPermanent(gameData, p, predicate, ctx));
    }

    private boolean noPlayerHasCardsInHand(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand != null && !hand.isEmpty()) {
                return false;
            }
        }
        return true;
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

    private boolean isAnyOpponentHandEmpty(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                return true;
            }
        }
        return false;
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

    private boolean wasAnyOpponentDealtDamageThisTurn(GameData gameData, UUID controllerId, int minimumAmount) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            int dealt = gameData.damageDealtToPlayersThisTurn.getOrDefault(playerId, 0);
            if (dealt >= Math.max(1, minimumAmount)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if the source permanent dealt combat damage to an opponent of its current controller this
     * turn (Whirling Dervish). Reads the per-source combat-damage-to-players tracking and treats any
     * damaged player other than the source's current controller as an opponent.
     */
    private boolean sourceDealtDamageToOpponentThisTurn(GameData gameData, ConditionContext ctx) {
        if (ctx.sourcePermanentId() == null || ctx.controllerId() == null) return false;
        Set<UUID> damagedPlayers = gameData.combatDamageToPlayersThisTurn.get(ctx.sourcePermanentId());
        if (damagedPlayers == null) return false;
        return damagedPlayers.stream().anyMatch(playerId -> !playerId.equals(ctx.controllerId()));
    }

    private boolean didAnyOpponentLoseLifeThisTurn(GameData gameData, UUID controllerId, int minimumAmount) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            int lost = gameData.lifeLostThisTurn.getOrDefault(playerId, 0);
            if (lost >= Math.max(1, minimumAmount)) {
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
            return source.getCard().getSubtypes().contains(subtype)
                    || source.getGrantedSubtypes().contains(subtype);
        }
        return ctx.sourceCard() != null && ctx.sourceCard().getSubtypes().contains(subtype);
    }

    private int countCardsInLibrary(GameData gameData, UUID controllerId) {
        if (controllerId == null) return 0;
        List<Card> deck = gameData.playerDecks.get(controllerId);
        return deck == null ? 0 : deck.size();
    }

    private boolean anyLibraryAtMost(GameData gameData, int threshold) {
        return gameData.playerDecks.values().stream().anyMatch(deck -> deck.size() <= threshold);
    }

    private int countCardsInHand(GameData gameData, UUID controllerId) {
        if (controllerId == null) return 0;
        List<Card> hand = gameData.playerHands.get(controllerId);
        return hand == null ? 0 : hand.size();
    }

    private boolean isTopCardOfLibraryColor(GameData gameData, UUID controllerId, TopCardOfLibraryColor c) {
        if (controllerId == null) return false;
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) return false;
        return deck.getFirst().getColors().contains(c.color());
    }

    /**
     * Soulbond self-ETB intervening-if: source is unpaired and controller controls another unpaired creature.
     */
    private boolean canSoulbond(GameData gameData, ConditionContext ctx) {
        Permanent source = sourcePermanent(gameData, ctx);
        if (source == null || source.getPairedWithId() != null) {
            return false;
        }
        if (!isCreatureForCondition(gameData, source, ctx)) {
            return false;
        }
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent p : battlefield) {
            if (p.getId().equals(source.getId())) {
                continue;
            }
            if (p.getPairedWithId() == null && isCreatureForCondition(gameData, p, ctx)) {
                return true;
            }
        }
        return false;
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
        Card imprintedCard = gameData.getImprintedCard(source.getCard());
        return imprintedCard != null && imprintedCard.getName().equals(ctx.triggeringCard().getName());
    }
}
