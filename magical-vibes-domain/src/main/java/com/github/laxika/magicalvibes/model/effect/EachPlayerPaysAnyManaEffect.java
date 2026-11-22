package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player may pay any amount of mana": each player in turn is asked once, starting with the controller (join
 * forces: "starting with you") or with the active player (APNAP, CR 101.4). The step records the payments
 * ({@code StepKind.PAY_MANA}) after the last answer: "the total amount of mana paid this way" is
 * {@code AmountThisWay.manaPaid()}, "the amount of mana they paid this way" {@code AmountThisWay.manaPaidByYou()}.
 *
 * @param startingWithYou whether the controller is asked first, else the active player
 */
public record EachPlayerPaysAnyManaEffect(boolean startingWithYou) implements CardEffect {

    /** "Starting with you, each player may pay any amount of mana." */
    public static EachPlayerPaysAnyManaEffect joinForces() {
        return new EachPlayerPaysAnyManaEffect(true);
    }

    /** "Each player may pay any amount of mana." */
    public static EachPlayerPaysAnyManaEffect inApnapOrder() {
        return new EachPlayerPaysAnyManaEffect(false);
    }
}
