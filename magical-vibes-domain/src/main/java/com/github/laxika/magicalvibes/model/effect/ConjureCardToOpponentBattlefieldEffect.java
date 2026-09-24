package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures one copy of a registered card printing onto the opponent's battlefield.
 *
 * @param setCode the printing's set code
 * @param collectorNumber the printing's collector number
 * @param gift whether this effect gives the controller's opponent a Gift
 */
public record ConjureCardToOpponentBattlefieldEffect(
        String setCode,
        String collectorNumber,
        boolean gift
) implements CardEffect {

    public static ConjureCardToOpponentBattlefieldEffect gift(String setCode, String collectorNumber) {
        return new ConjureCardToOpponentBattlefieldEffect(setCode, collectorNumber, true);
    }
}
