package com.github.laxika.magicalvibes.model.effect;

/**
 * Rona, Tolarian Obliterator's damage trigger: that source's controller exiles a card from their hand at random, and
 * the ability's controller may put it onto the battlefield (a land) or cast it without paying its mana cost. The
 * source's controller is the controller of the stack entry's triggering permanent as the ability resolves, or its last
 * known controller (CR 608.2h), or the controller the entry recorded for a source that was never a permanent.
 */
public record DamageSourceControllerExilesRandomHandCardEffect() implements CardEffect {
}
