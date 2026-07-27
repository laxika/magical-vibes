package com.github.laxika.magicalvibes.service.combat.block;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.AttackOrBlockRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.BlockabilityRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.BlockingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEquippedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityContext.BlockDenial;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Declare-blockers legality (CR 509): whether a creature may block at all, and whether a given
 * blocker may block a given attacker. Owns evasion keywords (flying, fear, intimidate, skulk,
 * horsemanship, landwalk), blocking restrictions from statics and auras, board-wide
 * "X can't block Y" effects, and protection as it applies to blocking.
 *
 * <p>This service answers legality questions only — declaring blockers, ordering them, and the
 * resulting combat state belong to {@code CombatBlockService}.
 *
 * <p>Like {@link GameQueryService}, which it reads characteristics from, this service never
 * mutates game state. The one exception is the memoization inside a {@link BlockLegalityContext}
 * the caller owns: build one context per blocker × attacker sweep and discard it after any
 * game-state mutation.
 */
@Component
@RequiredArgsConstructor
public class BlockLegalityService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;

    /**
     * Builds a standalone instance for callers that assemble their own service graph from a
     * {@link GameQueryService} rather than receiving Spring-injected beans — the AI decision
     * engines and the headless simulator. Mirrors {@code GameSimulator.forQueryService}.
     */
    public static BlockLegalityService forQueryService(GameQueryService gameQueryService) {
        PredicateEvaluationService predicates = new PredicateEvaluationService(gameQueryService);
        return new BlockLegalityService(gameQueryService, predicates,
                new ConditionEvaluationService(gameQueryService, predicates,
                        new StaticEffectSupport(gameQueryService, predicates)));
    }

    /**
     * Returns {@code true} if the given creature permanent can legally be declared as a blocker.
     */
    public boolean canBlock(GameData gameData, Permanent creature) {
        return gameQueryService.isCreature(gameData, creature)
                && !creature.isTapped()
                && !creature.isCantBlockThisTurn()
                && !gameQueryService.hasKeyword(gameData, creature, Keyword.DECAYED)
                && creature.getCard().getEffects(EffectSlot.STATIC).stream().noneMatch(CantBlockEffect.class::isInstance)
                && !gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCantAttackOrBlockEffect.class)
                && !gameQueryService.hasAuraWithEffect(gameData, creature, CantBlockEffect.class)
                && !(creature.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(CantAttackOrBlockUnlessEquippedEffect.class::isInstance)
                        && !gameQueryService.isEquipped(gameData, creature))
                && !isCantBlockUnlessConditionUnmet(gameData, creature)
                && !hasGlobalCantAttackOrBlockRestriction(gameData, creature);
    }

    /**
     * Returns {@code true} if a board-wide "creatures matching X can't attack or block" restriction
     * (e.g. Kulrath Knight, Light of Day) applies to the given creature, evaluating each restriction's
     * predicate relative to the source permanent's controller. The attack side is enforced in
     * {@code CombatAttackService}.
     */
    private boolean hasGlobalCantAttackOrBlockRestriction(GameData gameData, Permanent creature) {
        boolean[] restricted = {false};
        gameData.forEachPermanent((playerId, source) -> {
            if (restricted[0]) {
                return;
            }
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AttackOrBlockRestrictionEffect restriction
                        && restriction.globallyCantAttackOrBlock() != null) {
                    FilterContext context = FilterContext.of(gameData)
                            .withSourceControllerId(playerId)
                            .withSourceCardId(source.getOriginalCard().getId());
                    if (predicateEvaluationService.matchesPermanentPredicate(creature, restriction.globallyCantAttackOrBlock(), context)) {
                        restricted[0] = true;
                    }
                }
            }
        });
        return restricted[0];
    }

    /**
     * Returns {@code true} if the creature has a "can't [attack or] block unless …" restriction whose
     * condition is not met (block side, mirrors the attack side in {@code CombatAttackService}). Covers
     * both the combined {@code CantAttackOrBlockUnlessEffect} and the block-only
     * {@code CantBlockUnlessEffect}.
     */
    private boolean isCantBlockUnlessConditionUnmet(GameData gameData, Permanent creature) {
        UUID controllerId = null;
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            Condition unless = null;
            if (effect instanceof AttackOrBlockRestrictionEffect restriction) {
                unless = restriction.cantAttackOrBlockUnless();
            } else if (effect instanceof BlockingRestrictionEffect restriction) {
                unless = restriction.cantBlockUnless();
            }
            if (unless == null) {
                continue;
            }
            if (controllerId == null) {
                controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
                if (controllerId == null) return false;
            }
            if (!conditionEvaluationService.isMet(gameData, unless,
                    ConditionContext.forPermanent(creature, controllerId))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a {@link BlockLegalityContext} for one declare-blockers computation: collects the
     * board-wide block restrictions and defender land types once, then caches per-creature
     * facts as pairs are queried. Use one context for a whole blocker × attacker sweep and
     * build a new one after any game-state mutation.
     */
    public BlockLegalityContext createBlockLegalityContext(GameData gameData, List<Permanent> defenderBattlefield) {
        List<MatchingCreaturesCantBlockMatchingCreaturesEffect> globalRestrictions = new ArrayList<>();
        gameData.forEachPermanent((playerId, source) -> {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof MatchingCreaturesCantBlockMatchingCreaturesEffect restriction) {
                    globalRestrictions.add(restriction);
                }
            }
        });
        Set<CardSubtype> defenderCardSubtypes = EnumSet.noneOf(CardSubtype.class);
        if (defenderBattlefield != null) {
            for (Permanent p : defenderBattlefield) {
                defenderCardSubtypes.addAll(p.getCard().getSubtypes());
            }
        }
        return new BlockLegalityContext(gameData, defenderBattlefield, globalRestrictions, defenderCardSubtypes);
    }

    /**
     * Returns {@code true} if the given blocker can legally block the given attacker,
     * considering all evasion abilities, blocking restrictions, landwalk, and protection.
     * Builds a fresh single-use {@link BlockLegalityContext}; pairwise sweeps should build
     * one context via {@link #createBlockLegalityContext} and use the context overload.
     */
    public boolean canBlockAttacker(GameData gameData, Permanent blocker, Permanent attacker,
                                    List<Permanent> defenderBattlefield) {
        return canBlockAttacker(createBlockLegalityContext(gameData, defenderBattlefield), blocker, attacker);
    }

    /** Pairwise block legality against a shared context — the allocation-free fast path. */
    public boolean canBlockAttacker(BlockLegalityContext context, Permanent blocker, Permanent attacker) {
        return findBlockDenial(context, blocker, attacker) == null;
    }

    /**
     * Returns the reason a blocker cannot legally block the given attacker, or empty if the
     * block is legal. Builds a fresh single-use {@link BlockLegalityContext}.
     */
    public Optional<String> getBlockingIllegalityReason(GameData gameData, Permanent blocker,
                                                        Permanent attacker, List<Permanent> defenderBattlefield) {
        return getBlockingIllegalityReason(createBlockLegalityContext(gameData, defenderBattlefield), blocker, attacker);
    }

    /** Message form of {@link #canBlockAttacker(BlockLegalityContext, Permanent, Permanent)}. */
    public Optional<String> getBlockingIllegalityReason(BlockLegalityContext context, Permanent blocker, Permanent attacker) {
        BlockDenial denial = findBlockDenial(context, blocker, attacker);
        return denial == null ? Optional.empty() : Optional.of(formatBlockDenial(denial, blocker, attacker));
    }

    /**
     * The single source of truth for pairwise block legality: evasion keywords, blocking
     * restrictions, landwalk, and protection. Returns the failed rule, or {@code null} when
     * the block is legal. Creature-invariant facts come from the context caches so a
     * blocker × attacker sweep evaluates each side's board scans and layered-pass lookups
     * exactly once per creature; check order matches the pre-context implementation so the
     * surfaced message is unchanged when several rules fail at once.
     */
    private BlockDenial findBlockDenial(BlockLegalityContext context, Permanent blocker, Permanent attacker) {
        GameData gameData = context.gameData;
        BlockLegalityContext.AttackerFacts atk = context.attackerFacts.computeIfAbsent(
                attacker.getId(), id -> buildAttackerFacts(context, attacker));
        if (atk.unblockable()) {
            return BlockDenial.CANT_BE_BLOCKED;
        }
        BlockLegalityContext.BlockerFacts blk = context.blockerFacts.computeIfAbsent(
                blocker.getId(), id -> buildBlockerFacts(context, blocker));
        if (atk.flying() && !blk.flying() && !blk.reach()) {
            return BlockDenial.FLYING;
        }
        if (atk.horsemanship() && !blk.horsemanship()) {
            return BlockDenial.HORSEMANSHIP;
        }
        if (atk.fear() && !blk.artifact() && !blk.colors().contains(CardColor.BLACK)) {
            return BlockDenial.FEAR;
        }
        if (atk.intimidate() && !blk.artifact()
                && Collections.disjoint(blk.colors(), atk.colors())) {
            return BlockDenial.INTIMIDATE;
        }
        // Skulk: can't be blocked by creatures with greater power (CR 702.129a).
        if (atk.skulk() && gameQueryService.getEffectivePower(gameData, blocker) > gameQueryService.getEffectivePower(gameData, attacker)) {
            return BlockDenial.SKULK;
        }
        // Shrill Howler: creatures with power less than this creature's power can't block it.
        if (atk.cantBeBlockedByLessPower()
                && gameQueryService.getEffectivePower(gameData, blocker) < gameQueryService.getEffectivePower(gameData, attacker)) {
            return BlockDenial.CANT_BE_BLOCKED_BY_LESS_POWER;
        }
        for (CanBlockOnlyIfAttackerMatchesPredicateEffect restriction : blk.attackerFilterRestrictions()) {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                return new BlockDenial(BlockDenial.Reason.BLOCKER_LIMITED_TO_ATTACKERS, restriction.allowedAttackersDescription());
            }
        }
        // Board-wide "creatures matching X can't block creatures matching Y" restrictions
        // (e.g. Boldwyr Intimidator: "Cowards can't block Warriors.").
        for (MatchingCreaturesCantBlockMatchingCreaturesEffect restriction : context.globalBlockRestrictions) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                return new BlockDenial(BlockDenial.Reason.GLOBAL_RESTRICTION, restriction.description());
            }
        }
        for (CardEffect effect : atk.pairRestrictionStatics()) {
            if (effect instanceof BlockabilityRestrictionEffect restriction) {
                if (restriction.blockableOnlyBy() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockableOnlyBy())) {
                    return new BlockDenial(BlockDenial.Reason.ATTACKER_LIMITED_TO_BLOCKERS, restriction.blockableOnlyByDescription());
                }
                if (restriction.cantBeBlockedByCreaturesMatching() != null
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.cantBeBlockedByCreaturesMatching())) {
                    PermanentPredicate onlyIfDefenderControls =
                            restriction.cantBeBlockedByCreaturesMatchingOnlyIfDefenderControls();
                    if (onlyIfDefenderControls == null
                            || (context.defenderBattlefield != null && context.defenderBattlefield.stream()
                                .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(
                                        gameData, p, onlyIfDefenderControls)))) {
                        return BlockDenial.CANT_BE_BLOCKED_BY_MATCHING;
                    }
                }
            }
        }
        for (BlockabilityRestrictionEffect restriction : atk.auraGrantedRestrictions()) {
            if (restriction.blockableOnlyBy() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockableOnlyBy())) {
                return new BlockDenial(BlockDenial.Reason.ATTACKER_LIMITED_TO_BLOCKERS, restriction.blockableOnlyByDescription());
            }
            if (restriction.cantBeBlockedByCreaturesMatching() != null
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.cantBeBlockedByCreaturesMatching())) {
                PermanentPredicate onlyIfDefenderControls =
                        restriction.cantBeBlockedByCreaturesMatchingOnlyIfDefenderControls();
                if (onlyIfDefenderControls == null
                        || (context.defenderBattlefield != null && context.defenderBattlefield.stream()
                            .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, p, onlyIfDefenderControls)))) {
                    return BlockDenial.CANT_BE_BLOCKED_BY_MATCHING;
                }
            }
        }
        for (CanBeBlockedOnlyByFilterEffect restriction : attacker.getBlockRestrictionsUntilEndOfTurn()) {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return new BlockDenial(BlockDenial.Reason.ATTACKER_LIMITED_TO_BLOCKERS, restriction.allowedBlockersDescription());
            }
        }
        if (atk.landwalkDenial() != null) {
            return atk.landwalkDenial();
        }
        if (blocker.isCantBlockThisTurn()) {
            return BlockDenial.CANT_BLOCK_THIS_TURN;
        }
        if (gameQueryService.isLockedFromBlocking(gameData, blocker.getId())) {
            return BlockDenial.CANT_BLOCK;
        }
        if (blk.cantBlock()) {
            return BlockDenial.CANT_BLOCK;
        }
        // Ironclaw Curse: can't block attackers whose power >= this creature's own toughness.
        if (blk.cantBlockPowerAtLeastOwnToughness()
                && gameQueryService.getEffectivePower(gameData, attacker) >= gameQueryService.getEffectiveToughness(gameData, blocker)) {
            return BlockDenial.CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS;
        }
        // Ironclaw Orcs: can't block attackers whose power >= a fixed threshold.
        if (blk.cantBlockPowerAtLeast() != null
                && gameQueryService.getEffectivePower(gameData, attacker) >= blk.cantBlockPowerAtLeast()) {
            return BlockDenial.CANT_BLOCK_HIGH_POWER;
        }
        if (blocker.getCantBlockIds().contains(attacker.getId())) {
            return BlockDenial.CANT_BLOCK_THAT_ATTACKER;
        }
        if (gameQueryService.hasProtectionFromSource(gameData, attacker, blocker, blk.colors())) {
            return BlockDenial.PROTECTION;
        }
        return null;
    }

    private BlockLegalityContext.AttackerFacts buildAttackerFacts(BlockLegalityContext context, Permanent attacker) {
        GameData gameData = context.gameData;
        boolean unblockable = gameQueryService.hasCantBeBlocked(gameData, attacker);
        List<CardEffect> pairRestrictionStatics = null;
        boolean cantBeBlockedByLessPower = false;
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof BlockabilityRestrictionEffect restriction) {
                if (!unblockable) {
                    // Defender-condition unblockable (e.g. "can't be blocked if defending player controls a Forest")
                    if (restriction.unblockableIfDefenderControls() != null
                            && context.defenderBattlefield != null && context.defenderBattlefield.stream()
                                .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, restriction.unblockableIfDefenderControls()))) {
                        unblockable = true;
                    }
                    if (restriction.unblockableIfControllerCastHistoricSpellThisTurn()) {
                        UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
                        if (controllerId != null && gameQueryService.playerCastHistoricSpellThisTurn(gameData, controllerId)) {
                            unblockable = true;
                        }
                    }
                    if (restriction.unblockableWhileAttackingAlone() && isAttackingAlone(gameData, attacker)) {
                        unblockable = true;
                    }
                }
                if (restriction.cantBeBlockedByCreaturesWithLessPower()) {
                    cantBeBlockedByLessPower = true;
                }
                if (restriction.blockableOnlyBy() != null || restriction.cantBeBlockedByCreaturesMatching() != null) {
                    if (pairRestrictionStatics == null) {
                        pairRestrictionStatics = new ArrayList<>(2);
                    }
                    pairRestrictionStatics.add(effect);
                }
            }
        }
        GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, attacker);
        boolean intimidate = gameQueryService.hasKeyword(attacker, bonus, Keyword.INTIMIDATE);
        BlockDenial landwalkDenial = null;
        for (var entry : Keyword.LANDWALK_MAP.entrySet()) {
            if (gameQueryService.hasKeyword(attacker, bonus, entry.getKey())
                    && context.defenderCardSubtypes.contains(entry.getValue())) {
                landwalkDenial = new BlockDenial(BlockDenial.Reason.LANDWALK,
                        entry.getValue().getDisplayName().toLowerCase());
                break;
            }
        }
        return new BlockLegalityContext.AttackerFacts(
                unblockable,
                gameQueryService.hasKeyword(attacker, bonus, Keyword.FLYING),
                gameQueryService.hasKeyword(attacker, bonus, Keyword.HORSEMANSHIP),
                gameQueryService.hasKeyword(attacker, bonus, Keyword.FEAR),
                intimidate,
                gameQueryService.hasKeyword(attacker, bonus, Keyword.SKULK),
                cantBeBlockedByLessPower,
                intimidate ? gameQueryService.getEffectiveColors(gameData, attacker) : Set.of(),
                pairRestrictionStatics == null ? List.of() : pairRestrictionStatics,
                getAuraGrantedBlockingRestrictions(gameData, attacker),
                landwalkDenial);
    }

    private BlockLegalityContext.BlockerFacts buildBlockerFacts(BlockLegalityContext context, Permanent blocker) {
        GameData gameData = context.gameData;
        GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, blocker);
        List<CanBlockOnlyIfAttackerMatchesPredicateEffect> attackerFilterRestrictions = null;
        boolean cantBlockStatic = false;
        boolean cantBlockPowerAtLeastOwnToughnessStatic = false;
        Integer cantBlockPowerAtLeast = null;
        for (CardEffect effect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBlockOnlyIfAttackerMatchesPredicateEffect restriction) {
                if (attackerFilterRestrictions == null) {
                    attackerFilterRestrictions = new ArrayList<>(2);
                }
                attackerFilterRestrictions.add(restriction);
            }
            if (effect instanceof BlockingRestrictionEffect restriction) {
                if (restriction.cantBlock()) {
                    cantBlockStatic = true;
                }
                if (restriction.cantBlockCreaturesWithPowerAtLeastOwnToughness()) {
                    cantBlockPowerAtLeastOwnToughnessStatic = true;
                }
                Integer threshold = restriction.cantBlockCreaturesWithPowerAtLeast();
                if (threshold != null && (cantBlockPowerAtLeast == null || threshold < cantBlockPowerAtLeast)) {
                    cantBlockPowerAtLeast = threshold;
                }
            }
        }
        return new BlockLegalityContext.BlockerFacts(
                gameQueryService.hasKeyword(blocker, bonus, Keyword.FLYING),
                gameQueryService.hasKeyword(blocker, bonus, Keyword.REACH),
                gameQueryService.hasKeyword(blocker, bonus, Keyword.HORSEMANSHIP),
                gameQueryService.isArtifact(blocker),
                gameQueryService.getEffectiveColors(gameData, blocker),
                attackerFilterRestrictions == null ? List.of() : attackerFilterRestrictions,
                cantBlockStatic || gameQueryService.hasAuraWithEffect(gameData, blocker, CantBlockEffect.class)
                        || gameQueryService.hasKeyword(gameData, blocker, Keyword.DECAYED)
                        || hasGlobalCantAttackOrBlockRestriction(gameData, blocker),
                cantBlockPowerAtLeastOwnToughnessStatic || gameQueryService.hasAuraWithEffect(gameData, blocker,
                        CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect.class),
                cantBlockPowerAtLeast);
    }

    /** True if the given attacker is the only creature its controller declared as an attacker (CR 509.1). */
    private boolean isAttackingAlone(GameData gameData, Permanent attacker) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream().filter(Permanent::isAttacking).count() == 1;
    }

    /** Rebuilds the exact pre-context user-facing message for a failed block-legality check. */
    private static String formatBlockDenial(BlockDenial denial, Permanent blocker, Permanent attacker) {
        String blockerName = blocker.getCard().getName();
        String attackerName = attacker.getCard().getName();
        return switch (denial.reason()) {
            case CANT_BE_BLOCKED -> attackerName + " can't be blocked";
            case FLYING -> blockerName + " cannot block " + attackerName + " (flying)";
            case HORSEMANSHIP -> blockerName + " cannot block " + attackerName + " (horsemanship)";
            case FEAR -> blockerName + " cannot block " + attackerName + " (fear)";
            case INTIMIDATE -> blockerName + " cannot block " + attackerName + " (intimidate)";
            case SKULK -> blockerName + " cannot block " + attackerName + " (skulk)";
            case BLOCKER_LIMITED_TO_ATTACKERS -> blockerName + " can only block " + denial.detail();
            case GLOBAL_RESTRICTION -> denial.detail();
            case ATTACKER_LIMITED_TO_BLOCKERS -> attackerName + " can only be blocked by " + denial.detail();
            case CANT_BE_BLOCKED_BY_MATCHING -> blockerName + " cannot block " + attackerName;
            case CANT_BE_BLOCKED_BY_LESS_POWER ->
                    blockerName + " cannot block " + attackerName + " (power too low)";
            case LANDWALK -> attackerName + " can't be blocked (" + denial.detail() + "walk)";
            case CANT_BLOCK_THIS_TURN -> blockerName + " can't block this turn";
            case CANT_BLOCK -> blockerName + " can't block";
            case CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS ->
                    blockerName + " can't block " + attackerName + " (power too high)";
            case CANT_BLOCK_HIGH_POWER ->
                    blockerName + " can't block " + attackerName + " (power too high)";
            case CANT_BLOCK_THAT_ATTACKER -> blockerName + " can't block " + attackerName + " this turn";
            case PROTECTION -> blockerName + " cannot block " + attackerName + " (protection)";
        };
    }

    private List<BlockabilityRestrictionEffect> getAuraGrantedBlockingRestrictions(GameData gameData, Permanent creature) {
        List<BlockabilityRestrictionEffect> restrictions = new ArrayList<>();
        gameData.forEachPermanent((playerId, aura) -> {
            if (!aura.isAttached() || !aura.getAttachedTo().equals(creature.getId())) {
                return;
            }
            for (CardEffect effect : aura.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof BlockabilityRestrictionEffect restriction
                        && (restriction.blockableOnlyBy() != null
                        || restriction.cantBeBlockedByCreaturesMatching() != null)) {
                    restrictions.add(restriction);
                }
            }
        });
        return restrictions;
    }
}
