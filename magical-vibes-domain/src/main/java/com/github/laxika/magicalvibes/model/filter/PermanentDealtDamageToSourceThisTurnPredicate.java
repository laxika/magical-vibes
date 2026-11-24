package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that dealt damage to the source permanent this turn ("each creature that dealt damage to it this
 * turn", Brine Hag). The source is judged as it last existed when it has left the battlefield: a death trigger reads
 * the dying source's damage record (CR 603.10a). The reverse of {@link PermanentDealtDamageThisTurnBySourcePredicate}.
 */
public record PermanentDealtDamageToSourceThisTurnPredicate() implements PermanentPredicate {
}
