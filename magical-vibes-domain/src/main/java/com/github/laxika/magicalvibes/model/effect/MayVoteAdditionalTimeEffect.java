package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker for "While voting, you may vote an additional time" (Tivit, Seller of Secrets; The
 * Valeyard). Each copy controlled by a voter lets that player choose, after each of their votes, to vote
 * once more in every vote they take part in; the extra vote happens at the same time as their normal
 * vote (CR 701.38d).
 */
public record MayVoteAdditionalTimeEffect() implements CardEffect {
}
