package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a card in their hand, then all chosen cards are revealed. The owners of
 * the creature cards with the lowest mana value among those cards put them onto the battlefield.
 */
public record StrongholdGambitEffect() implements CardEffect {
}
