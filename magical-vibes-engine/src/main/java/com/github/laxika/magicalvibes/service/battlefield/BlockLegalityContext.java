package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Per-computation cache for pairwise block-legality checks. Built by
 * {@link GameQueryService#createBlockLegalityContext} for one defender battlefield and one
 * unmutated game state, then shared across every blocker × attacker query of that computation
 * so board scans and layered-pass lookups run once per creature instead of once per pair.
 *
 * <p>The cached facts snapshot the game state at first query: do not mutate game state
 * (blocks, attachments, keywords, battlefields) while a context is in use — build a new
 * context after any mutation. The four-argument
 * {@link GameQueryService#canBlockAttacker(GameData, Permanent, Permanent, List)} form remains
 * available for one-off checks; it builds a fresh single-use context internally.
 */
public final class BlockLegalityContext {

    final GameData gameData;
    final List<Permanent> defenderBattlefield;

    /** Board-wide "X can't block Y" statics (e.g. Boldwyr Intimidator), collected once. */
    final List<MatchingCreaturesCantBlockMatchingCreaturesEffect> globalBlockRestrictions;

    /** Union of printed card subtypes on the defender battlefield, for landwalk checks. */
    final Set<CardSubtype> defenderCardSubtypes;

    final Map<UUID, AttackerFacts> attackerFacts = new HashMap<>();
    final Map<UUID, BlockerFacts> blockerFacts = new HashMap<>();

    BlockLegalityContext(GameData gameData, List<Permanent> defenderBattlefield,
                         List<MatchingCreaturesCantBlockMatchingCreaturesEffect> globalBlockRestrictions,
                         Set<CardSubtype> defenderCardSubtypes) {
        this.gameData = gameData;
        this.defenderBattlefield = defenderBattlefield;
        this.globalBlockRestrictions = globalBlockRestrictions;
        this.defenderCardSubtypes = defenderCardSubtypes;
    }

    /**
     * Attacker-side facts that do not depend on the blocker, computed once per attacker.
     * {@code colors} is populated only when the attacker has intimidate (the only check that
     * reads the attacker's colors). {@code landwalkDenial} is the prebuilt denial when a
     * landwalk keyword matches a defender land type, or {@code null}.
     */
    record AttackerFacts(boolean unblockable,
                         boolean flying,
                         boolean horsemanship,
                         boolean fear,
                         boolean intimidate,
                         boolean skulk,
                         Set<CardColor> colors,
                         List<CardEffect> pairRestrictionStatics,
                         List<CanBeBlockedOnlyByFilterEffect> auraGrantedRestrictions,
                         BlockDenial landwalkDenial) {
    }

    /** Blocker-side facts that do not depend on the attacker, computed once per blocker. */
    record BlockerFacts(boolean flying,
                        boolean reach,
                        boolean horsemanship,
                        boolean artifact,
                        Set<CardColor> colors,
                        List<CanBlockOnlyIfAttackerMatchesPredicateEffect> attackerFilterRestrictions,
                        boolean cantBlock,
                        boolean cantBlockPowerAtLeastOwnToughness,
                        Integer cantBlockPowerAtLeast) {
    }

    /**
     * A failed block-legality check: the rule that failed plus the detail needed to rebuild
     * the exact user-facing message (a restriction description, or the land name for
     * landwalk). Detail-less reasons reuse the shared instances below so the boolean fast
     * path allocates nothing per pair.
     */
    record BlockDenial(Reason reason, String detail) {

        enum Reason {
            CANT_BE_BLOCKED,
            FLYING,
            HORSEMANSHIP,
            FEAR,
            INTIMIDATE,
            SKULK,
            BLOCKER_LIMITED_TO_ATTACKERS,
            GLOBAL_RESTRICTION,
            ATTACKER_LIMITED_TO_BLOCKERS,
            CANT_BE_BLOCKED_BY_MATCHING,
            LANDWALK,
            CANT_BLOCK_THIS_TURN,
            CANT_BLOCK,
            CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS,
            CANT_BLOCK_HIGH_POWER,
            CANT_BLOCK_THAT_ATTACKER,
            PROTECTION
        }

        static final BlockDenial CANT_BE_BLOCKED = new BlockDenial(Reason.CANT_BE_BLOCKED, null);
        static final BlockDenial FLYING = new BlockDenial(Reason.FLYING, null);
        static final BlockDenial HORSEMANSHIP = new BlockDenial(Reason.HORSEMANSHIP, null);
        static final BlockDenial FEAR = new BlockDenial(Reason.FEAR, null);
        static final BlockDenial INTIMIDATE = new BlockDenial(Reason.INTIMIDATE, null);
        static final BlockDenial SKULK = new BlockDenial(Reason.SKULK, null);
        static final BlockDenial CANT_BE_BLOCKED_BY_MATCHING = new BlockDenial(Reason.CANT_BE_BLOCKED_BY_MATCHING, null);
        static final BlockDenial CANT_BLOCK_THIS_TURN = new BlockDenial(Reason.CANT_BLOCK_THIS_TURN, null);
        static final BlockDenial CANT_BLOCK = new BlockDenial(Reason.CANT_BLOCK, null);
        static final BlockDenial CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS =
                new BlockDenial(Reason.CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS, null);
        static final BlockDenial CANT_BLOCK_HIGH_POWER =
                new BlockDenial(Reason.CANT_BLOCK_HIGH_POWER, null);
        static final BlockDenial CANT_BLOCK_THAT_ATTACKER = new BlockDenial(Reason.CANT_BLOCK_THAT_ATTACKER, null);
        static final BlockDenial PROTECTION = new BlockDenial(Reason.PROTECTION, null);
    }
}
