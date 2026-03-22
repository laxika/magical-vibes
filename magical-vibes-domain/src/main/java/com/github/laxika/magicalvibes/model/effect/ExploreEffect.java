package com.github.laxika.magicalvibes.model.effect;

/**
 * The source creature explores: reveal the top card of your library.
 * If it's a land card, put it into your hand.
 * Otherwise, put a +1/+1 counter on this creature, then you may put that card
 * into your graveyard (if you don't, it stays on top of your library).
 *
 * <p>Used by Brazen Buccaneers and other Ixalan explore creatures.
 */
public record ExploreEffect() implements CardEffect {
}
