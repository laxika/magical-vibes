package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker on an Aura or Equipment: "the first time you would create one or more tokens each turn, you may
 * instead create that many tokens that are copies of [the permanent this is attached to]" (Mirrormind Crown, Moonlit
 * Meditation). A replacement of the token-creation event, offered once per turn per source while it is attached and
 * has its abilities; it applies before anything that changes how the tokens enter, so copies of tapped tokens enter
 * tapped. Read by {@code EsixTokenReplacementSupport.offerAttachedCopy}.
 */
public record FirstTokenCreationCopiesAttachedPermanentEffect() implements CardEffect {
}
