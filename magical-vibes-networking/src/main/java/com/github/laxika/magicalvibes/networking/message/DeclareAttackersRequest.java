package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.Map;

/**
 * Attacker declaration from the active player.
 *
 * @param attackerIndices battlefield indices of the creatures being declared as attackers
 * @param attackTargets   optional per-attacker attack target (player/planeswalker id as string)
 * @param bands           optional attacking bands (CR 702.22): each inner list is the set of
 *                        attacker indices grouped into one band. May be null/empty.
 */
public record DeclareAttackersRequest(List<Integer> attackerIndices, Map<Integer, String> attackTargets,
                                      List<List<Integer>> bands) {

    /** Backwards-compatible constructor for callers that declare no bands. */
    public DeclareAttackersRequest(List<Integer> attackerIndices, Map<Integer, String> attackTargets) {
        this(attackerIndices, attackTargets, null);
    }
}
