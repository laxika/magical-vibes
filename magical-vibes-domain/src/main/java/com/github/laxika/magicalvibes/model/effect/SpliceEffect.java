package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static effect declaring that a card has splice — an optional additional cost paid while casting
 * a spell that meets the quality requirement (CR 702.46).
 *
 * <p>"Splice onto [quality] [cost]" means: as you cast a spell with that quality (e.g. Arcane),
 * you may reveal this card from your hand and pay the splice cost. If you do, this card's effects
 * are added to that spell; the spliced card remains in your hand.
 *
 * @param ontoSubtype the subtype the host spell must have (e.g. {@link CardSubtype#ARCANE})
 * @param cost        the splice mana cost (e.g. "{2}{R}{R}")
 */
public record SpliceEffect(CardSubtype ontoSubtype, String cost) implements CardEffect {
}
