package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top five cards of your library. As many times as you choose, you may pay 1 life,
 * put those cards on the bottom of your library in any order, then look at the top five cards of
 * your library. Then shuffle and put the last cards you looked at this way on top in any order.
 *
 * <p>Resolution holds the looked-at cards out of the library and drives a loop of
 * {@link com.github.laxika.magicalvibes.model.PendingInteraction.LimDulsVaultRepeatChoice}
 * accept/decline prompts, each accepted repeat paying 1 life and ordering the held cards onto the
 * bottom via a {@link com.github.laxika.magicalvibes.model.PendingInteraction.LimDulsVaultOrderChoice}.
 * Declining shuffles the library and orders the last cards looked at back on top. Used by
 * Lim-Dûl's Vault.
 */
public record LimDulsVaultEffect() implements CardEffect {
}
