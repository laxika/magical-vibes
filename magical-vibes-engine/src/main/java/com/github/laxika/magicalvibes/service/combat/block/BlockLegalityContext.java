package com.github.laxika.magicalvibes.service.combat.block;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BlockabilityRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Per-computation cache for block-legality checks. Built by
 * {@link BlockLegalityService#createBlockLegalityContext} for one defender battlefield and one
 * unmutated game state, then shared across every query of that computation so the board scans and
 * layered-pass lookups run once per context (for the board-wide restrictions) or once per creature
 * (for that creature's own characteristics) instead of once per question asked.
 *
 * <p>The cached facts snapshot the game state at first query: do not mutate game state
 * (blocks, attachments, keywords, battlefields) while a context is in use — build a new
 * context after any mutation. The {@link GameData}-taking overloads of
 * {@link BlockLegalityService#canBlock} and {@link BlockLegalityService#canBlockAttacker} remain
 * available for one-off checks; they build a fresh single-use context internally.
 */
public final class BlockLegalityContext {

    final GameData gameData;

    /** The defending player's battlefield; empty rather than {@code null} when there is none. */
    final List<Permanent> defenderBattlefield;

    /** Board-wide "X can't block Y" statics (e.g. Boldwyr Intimidator), collected once. */
    final List<GlobalBlockRestriction> globalBlockRestrictions;

    /** Board-wide "creatures matching X can't attack or block" statics, collected once. */
    final List<GlobalAttackOrBlockRestriction> globalAttackOrBlockRestrictions;

    /**
     * Every attached permanent on the board, bucketed by the id of what it is attached to, so the
     * per-creature aura lookups are a map read instead of a board scan. Creatures with nothing
     * attached are absent rather than mapped to an empty list.
     */
    final Map<UUID, List<Permanent>> attachedByHostId;

    /** Union of printed card subtypes on the defender battlefield, for landwalk checks. */
    final Set<CardSubtype> defenderCardSubtypes;

    /**
     * True while a permanent on the board switches landwalk off (Staff of the Ages): landwalk
     * abilities (CR 702.14a) are ignored when checking blocks, everything else still applies.
     */
    final boolean landwalkIgnored;

    final Map<UUID, AttackerFacts> attackerFacts = new HashMap<>();
    final Map<UUID, BlockerFacts> blockerFacts = new HashMap<>();

    /**
     * Memoized "does this creature currently fail a can't-block-unless requirement" answers. Kept
     * apart from {@link BlockerFacts} because only {@link BlockLegalityService#canBlock} asks — a
     * pairwise sweep must not pay for the condition evaluation and aura scan behind it.
     */
    final Map<UUID, Boolean> blockRequirementUnmet = new HashMap<>();

    BlockLegalityContext(GameData gameData,
                         List<Permanent> defenderBattlefield,
                         List<GlobalBlockRestriction> globalBlockRestrictions,
                         List<GlobalAttackOrBlockRestriction> globalAttackOrBlockRestrictions,
                         Map<UUID, List<Permanent>> attachedByHostId,
                         Set<CardSubtype> defenderCardSubtypes,
                         boolean landwalkIgnored) {
        this.gameData = gameData;
        this.defenderBattlefield = defenderBattlefield;
        this.globalBlockRestrictions = globalBlockRestrictions;
        this.globalAttackOrBlockRestrictions = globalAttackOrBlockRestrictions;
        this.attachedByHostId = attachedByHostId;
        this.defenderCardSubtypes = defenderCardSubtypes;
        this.landwalkIgnored = landwalkIgnored;
    }

    /** The attached permanents on {@code host}, or an empty list when it has none. */
    List<Permanent> attachedTo(Permanent host) {
        return attachedByHostId.getOrDefault(host.getId(), List.of());
    }

    /**
     * One board-wide "creatures matching X can't block creatures matching Y" static, paired with the
     * {@link FilterContext} of the permanent that imposes it — either side's predicate may be
     * controller-relative (Bower Passage: "creatures you control"), so both are evaluated against the
     * source's controller rather than bare game data.
     */
    record GlobalBlockRestriction(MatchingCreaturesCantBlockMatchingCreaturesEffect effect,
                                  FilterContext filterContext) {
    }

    /**
     * One board-wide "creatures matching this predicate can't attack or block" static, paired with
     * the {@link FilterContext} of the permanent that imposes it — prebuilt, since the predicate is
     * evaluated relative to the source's controller for every creature the sweep asks about.
     */
    record GlobalAttackOrBlockRestriction(PermanentPredicate predicate, FilterContext filterContext) {
    }

    /**
     * Attacker-side facts that do not depend on the blocker, computed once per attacker.
     * {@code colors} is populated only when the attacker has intimidate (the only check that
     * reads the attacker's colors). {@code pairRestrictions} holds the restrictions that need
     * the blocker to evaluate, printed statics first and then the ones granted by attached
     * auras — that order decides which message surfaces when several of them deny at once.
     * {@code landwalkDenial} is the prebuilt denial when a landwalk keyword matches a defender
     * land type, or {@code null}.
     */
    record AttackerFacts(boolean unblockable,
                         boolean flying,
                         boolean horsemanship,
                         boolean fear,
                         boolean intimidate,
                         boolean skulk,
                         boolean shadow,
                         boolean cantBeBlockedByLessPower,
                         Set<CardColor> colors,
                         List<BlockabilityRestrictionEffect> pairRestrictions,
                         BlockDenial landwalkDenial) {
    }

    /**
     * Blocker-side facts that do not depend on the attacker, computed once per blocker.
     *
     * <p>{@code prohibitedFromBlocking} is the flat "this creature can't block anything" verdict — a
     * "can't block" static or aura, decayed, or a board-wide restriction. It is the one creature-level
     * prohibition both {@link BlockLegalityService#canBlock} and the pairwise check consult, so the
     * two can never disagree about it. The other half of the unary question, a requirement the
     * creature currently fails ("can't block unless …"), is cached in
     * {@link BlockLegalityContext#blockRequirementUnmet} instead: no particular attacker changes the
     * answer, so a pairwise sweep never needs it.
     */
    record BlockerFacts(boolean flying,
                        boolean reach,
                        boolean horsemanship,
                        boolean shadow,
                        boolean artifact,
                        Set<CardColor> colors,
                        List<CanBlockOnlyIfAttackerMatchesPredicateEffect> attackerFilterRestrictions,
                        boolean prohibitedFromBlocking,
                        boolean cantBlockPowerAtLeastOwnToughness,
                        Integer cantBlockPowerAtLeast) {
    }
}
