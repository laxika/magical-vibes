package com.github.laxika.magicalvibes.model.effect;


/**
 * Exiles the top card of the controller's library and lets them play it until end of turn. When a
 * card is exiled this way, a reflexive trigger gives target creature the controller controls +X/+0
 * until end of turn, where X is that card's mana value (Cait Sith, Fortune Teller). The effect
 * itself doesn't target; the reflexive trigger chooses its target as it's put on the stack.
 */
public record ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect() implements CardEffect {
}
