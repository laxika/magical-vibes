package com.github.laxika.magicalvibes.model.effect;

/**
 * The defending player discards their entire hand, then draws that many cards. The defending
 * player is read from the combat trigger's {@code attackedTargetId}.
 */
public record DefendingPlayerDiscardsHandThenDrawsThatManyEffect() implements CardEffect {
}
