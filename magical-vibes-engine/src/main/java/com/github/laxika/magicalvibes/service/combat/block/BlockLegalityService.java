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
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityContext.GlobalAttackOrBlockRestriction;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * resulting combat state belong to {@code CombatBlockService}; wording the refusal belongs to
 * {@link BlockDenialMessageService}.
 *
 * <p>Like {@link GameQueryService}, which it reads characteristics from, this service never
 * mutates game state. The one exception is the memoization inside a {@link BlockLegalityContext}
 * the caller owns: build one context per declare-blockers computation and discard it after any
 * game-state mutation.
 */
@Component
@RequiredArgsConstructor
public class BlockLegalityService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final BlockDenialMessageService blockDenialMessageService;

    /**
     * Builds a standalone instance for callers that assemble their own service graph from a
     * {@link GameQueryService} rather than receiving Spring-injected beans — the AI decision
     * engines and the headless simulator. Mirrors {@code GameSimulator.forQueryService}.
     */
    public static BlockLegalityService forQueryService(GameQueryService gameQueryService) {
        PredicateEvaluationService predicates = new PredicateEvaluationService(gameQueryService);
        return new BlockLegalityService(gameQueryService, predicates,
                new ConditionEvaluationService(gameQueryService, predicates,
                        new StaticEffectSupport(gameQueryService, predicates)),
                new BlockDenialMessageService());
    }

    /**
     * Builds a {@link BlockLegalityContext} for one declare-blockers computation: collects the
     * board-wide restrictions, the attached auras, and the defender land types once, then caches
     * per-creature facts as creatures are queried. Use one context for a whole sweep and build a
     * new one after any game-state mutation. A {@code null} defender battlefield is read as an
     * empty one.
     */
    public BlockLegalityContext createBlockLegalityContext(GameData gameData, List<Permanent> defenderBattlefield) {
        List<MatchingCreaturesCantBlockMatchingCreaturesEffect> globalBlockRestrictions = new ArrayList<>();
        List<GlobalAttackOrBlockRestriction> globalAttackOrBlockRestrictions = new ArrayList<>();
        Map<UUID, List<Permanent>> attachedByHostId = new HashMap<>();
        gameData.forEachPermanent((playerId, source) -> {
            if (source.isAttached()) {
                attachedByHostId.computeIfAbsent(source.getAttachedTo(), id -> new ArrayList<>(1)).add(source);
            }
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof MatchingCreaturesCantBlockMatchingCreaturesEffect restriction) {
                    globalBlockRestrictions.add(restriction);
                }
                if (effect instanceof AttackOrBlockRestrictionEffect restriction
                        && restriction.globallyCantAttackOrBlock() != null) {
                    globalAttackOrBlockRestrictions.add(new GlobalAttackOrBlockRestriction(
                            restriction.globallyCantAttackOrBlock(),
                            FilterContext.of(gameData)
                                    .withSourceControllerId(playerId)
                                    .withSourceCardId(source.getOriginalCard().getId())));
                }
            }
        });
        List<Permanent> defenders = defenderBattlefield == null ? List.of() : defenderBattlefield;
        Set<CardSubtype> defenderCardSubtypes = EnumSet.noneOf(CardSubtype.class);
        for (Permanent defender : defenders) {
            defenderCardSubtypes.addAll(defender.getCard().getSubtypes());
        }
        return new BlockLegalityContext(gameData, defenders, globalBlockRestrictions,
                globalAttackOrBlockRestrictions, attachedByHostId, defenderCardSubtypes);
    }

    /**
     * Returns {@code true} if the given creature permanent can legally be declared as a blocker.
     * Builds a fresh single-use {@link BlockLegalityContext}; sweeps over a battlefield should build
     * one context via {@link #createBlockLegalityContext} and use the context overload.
     */
    public boolean canBlock(GameData gameData, Permanent creature) {
        return canBlock(createBlockLegalityContext(gameData, List.of()), creature);
    }

    /**
     * Whether the given creature can legally be declared as a blocker, against a shared context.
     * Eligibility that a mid-sweep mutation would invalidate (being a creature at all, tapped,
     * stopped from blocking this turn) is read live; the effect-driven half comes from the context
     * caches, so a battlefield sweep scans the board once rather than once per creature.
     */
    public boolean canBlock(BlockLegalityContext context, Permanent creature) {
        if (!gameQueryService.isCreature(context.gameData, creature)
                || creature.isTapped()
                || creature.isCantBlockThisTurn()) {
            return false;
        }
        if (blockerFacts(context, creature).prohibitedFromBlocking()) {
            return false;
        }
        return !context.blockRequirementUnmet.computeIfAbsent(
                creature.getId(), id -> hasUnmetBlockRequirement(context, creature));
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
        return denial == null
                ? Optional.empty()
                : Optional.of(blockDenialMessageService.describe(denial, blocker, attacker));
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
        BlockLegalityContext.BlockerFacts blk = blockerFacts(context, blocker);
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
        BlockDenial pairRestrictionDenial = findPairRestrictionDenial(context, blocker, atk.pairRestrictions());
        if (pairRestrictionDenial != null) {
            return pairRestrictionDenial;
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
        if (blk.prohibitedFromBlocking()) {
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

    /**
     * Applies the attacker's blocker-dependent {@link BlockabilityRestrictionEffect}s — "can be
     * blocked only by …" and "can't be blocked by …" — to one candidate blocker, whether the
     * restriction is printed on the attacker or granted by an aura attached to it. Returns the
     * first failing restriction, or {@code null} when the blocker satisfies all of them.
     */
    private BlockDenial findPairRestrictionDenial(BlockLegalityContext context, Permanent blocker,
                                                  List<BlockabilityRestrictionEffect> restrictions) {
        GameData gameData = context.gameData;
        for (BlockabilityRestrictionEffect restriction : restrictions) {
            if (restriction.blockableOnlyBy() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockableOnlyBy())) {
                return new BlockDenial(BlockDenial.Reason.ATTACKER_LIMITED_TO_BLOCKERS, restriction.blockableOnlyByDescription());
            }
            if (restriction.cantBeBlockedByCreaturesMatching() != null
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.cantBeBlockedByCreaturesMatching())) {
                PermanentPredicate onlyIfDefenderControls =
                        restriction.cantBeBlockedByCreaturesMatchingOnlyIfDefenderControls();
                if (onlyIfDefenderControls == null || defenderControls(context, onlyIfDefenderControls)) {
                    return BlockDenial.CANT_BE_BLOCKED_BY_MATCHING;
                }
            }
        }
        return null;
    }

    /** True if the defending player controls a permanent matching the given predicate. */
    private boolean defenderControls(BlockLegalityContext context, PermanentPredicate predicate) {
        return context.defenderBattlefield.stream()
                .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(context.gameData, p, predicate));
    }

    /**
     * Returns {@code true} if a board-wide "creatures matching X can't attack or block" restriction
     * (e.g. Kulrath Knight, Light of Day) applies to the given creature. Each restriction was
     * collected with the {@link FilterContext} of the permanent imposing it, so the predicate is
     * still evaluated relative to that source's controller. The attack side is enforced in
     * {@code CombatAttackService}.
     */
    private boolean hasGlobalCantAttackOrBlockRestriction(BlockLegalityContext context, Permanent creature) {
        for (GlobalAttackOrBlockRestriction restriction : context.globalAttackOrBlockRestrictions) {
            if (predicateEvaluationService.matchesPermanentPredicate(
                    creature, restriction.predicate(), restriction.filterContext())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the creature currently fails a requirement it must meet to block at all:
     * a "can't [attack or] block unless …" condition (block side, mirroring the attack side in
     * {@code CombatAttackService}), the equipped requirement, or an aura that stops it attacking or
     * blocking. Unlike a flat prohibition this is not consulted per attacker — no choice of attacker
     * makes an unmet requirement met.
     */
    private boolean hasUnmetBlockRequirement(BlockLegalityContext context, Permanent creature) {
        GameData gameData = context.gameData;
        if (gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCantAttackOrBlockEffect.class)) {
            return true;
        }
        if (creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockUnlessEquippedEffect.class::isInstance)
                && !gameQueryService.isEquipped(gameData, creature)) {
            return true;
        }
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

    private BlockLegalityContext.BlockerFacts blockerFacts(BlockLegalityContext context, Permanent blocker) {
        return context.blockerFacts.computeIfAbsent(blocker.getId(), id -> buildBlockerFacts(context, blocker));
    }

    private BlockLegalityContext.AttackerFacts buildAttackerFacts(BlockLegalityContext context, Permanent attacker) {
        GameData gameData = context.gameData;
        boolean unblockable = gameQueryService.hasCantBeBlocked(gameData, attacker);
        List<BlockabilityRestrictionEffect> pairRestrictions = new ArrayList<>(2);
        boolean cantBeBlockedByLessPower = false;
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof BlockabilityRestrictionEffect restriction) {
                if (!unblockable) {
                    // Defender-condition unblockable (e.g. "can't be blocked if defending player controls a Forest")
                    if (restriction.unblockableIfDefenderControls() != null
                            && defenderControls(context, restriction.unblockableIfDefenderControls())) {
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
                    pairRestrictions.add(restriction);
                }
            }
        }
        addAuraGrantedBlockingRestrictions(context, attacker, pairRestrictions);
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
                pairRestrictions,
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
                        || hasGlobalCantAttackOrBlockRestriction(context, blocker),
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

    /** Appends the blocker-dependent restrictions the auras attached to {@code creature} grant it. */
    private void addAuraGrantedBlockingRestrictions(BlockLegalityContext context, Permanent creature,
                                                    List<BlockabilityRestrictionEffect> restrictions) {
        for (Permanent aura : context.attachedTo(creature)) {
            for (CardEffect effect : aura.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof BlockabilityRestrictionEffect restriction
                        && (restriction.blockableOnlyBy() != null
                        || restriction.cantBeBlockedByCreaturesMatching() != null)) {
                    restrictions.add(restriction);
                }
            }
        }
    }
}
