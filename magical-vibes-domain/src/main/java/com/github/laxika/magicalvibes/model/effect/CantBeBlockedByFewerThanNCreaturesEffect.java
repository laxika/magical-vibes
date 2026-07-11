package com.github.laxika.magicalvibes.model.effect;

/**
 * Static evasion restriction on attackers (generalized menace).
 * This creature can't be blocked except by {@code minBlockers} or more creatures — if it's blocked,
 * it must be blocked by at least that many creatures. Menace is the {@code minBlockers == 2} case.
 * Guile uses {@code minBlockers == 3}.
 */
public record CantBeBlockedByFewerThanNCreaturesEffect(int minBlockers) implements CardEffect {
}
