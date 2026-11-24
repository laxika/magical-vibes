package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/** Which activated abilities a {@link ReduceActivatedAbilityCostEffect} makes cheaper. */
public enum ActivatedAbilityKind {

    /** Every activated ability. */
    ANY,

    /** Power-up abilities. */
    POWER_UP,

    /** Boast abilities ("Boast abilities you activate cost {1} less", Dragonkin Berserker). */
    BOAST,

    /** Exhaust abilities ("Exhaust abilities of other permanents you control", Boom Scholar). */
    EXHAUST,

    /** Abilities that aren't mana abilities ("Abilities you activate that aren't mana abilities", Zirda). */
    NON_MANA;

    /** Whether {@code ability} is of this kind. */
    public boolean matches(ActivatedAbility ability) {
        return switch (this) {
            case ANY -> true;
            case POWER_UP -> ability.isPowerUpAbility();
            case BOAST -> ability.isBoast();
            case EXHAUST -> ability.isExhaustAbility();
            case NON_MANA -> !ability.isManaAbility();
        };
    }
}
