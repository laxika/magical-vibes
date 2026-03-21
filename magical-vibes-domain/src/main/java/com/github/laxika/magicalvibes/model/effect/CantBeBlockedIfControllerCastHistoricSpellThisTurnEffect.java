package com.github.laxika.magicalvibes.model.effect;

/**
 * Static evasion effect: this creature can't be blocked as long as its controller
 * has cast a historic spell this turn (artifacts, legendaries, and Sagas).
 */
public record CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect() implements CardEffect {
}
