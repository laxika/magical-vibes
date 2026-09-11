package com.github.laxika.magicalvibes.model.amount;

/** The number of players whose graveyard contains at least {@code threshold} non-token cards. */
public record GraveyardsAtLeast(int threshold) implements DynamicAmount {
}
