package com.github.laxika.magicalvibes.model.effect;

/**
 * "Return this spell to its owner's hand, then discard a card at random." (Hanabi Blast)
 *
 * <p>The return happens <em>before</em> the discard, so the spell card itself is one of the cards
 * that can be discarded at random — with an empty hand it always discards itself. Because the
 * engine moves a resolved spell off the stack only after resolution finishes, the handler picks
 * uniformly among the hand plus the spell card and then decides the spell's disposition: hand when
 * another card was discarded, graveyard when the spell discarded itself.
 */
public record ReturnThisSpellToHandThenDiscardAtRandomEffect() implements CardEffect {
}
