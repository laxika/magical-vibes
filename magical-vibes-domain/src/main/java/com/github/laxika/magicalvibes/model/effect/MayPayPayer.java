package com.github.laxika.magicalvibes.model.effect;

/**
 * Which players are offered a non-mana {@link MayPayManaEffect#anyPlayerMay} payment, each in APNAP order (CR 101.4).
 * Every other "may pay" names its payer with a {@link PlayerSubject} on {@link UnlessPaysEffect}.
 */
public enum MayPayPayer {

    /** Any player ("any player may sacrifice …"). */
    ANY_PLAYER,

    /** Any opponent of the ability's controller ("unless an opponent sacrifices …"). */
    ANY_OPPONENT
}
