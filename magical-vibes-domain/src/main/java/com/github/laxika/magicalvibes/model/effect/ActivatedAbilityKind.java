package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * Which activated abilities a {@link ReduceActivatedAbilityCostEffect} makes cheaper, or a
 * {@link PayZeroRatherThanAbilityCostEffect} lets its controller activate for {0}.
 */
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
    NON_MANA,

    /** Equip abilities (CR 702.6a). */
    EQUIP,

    /** Cycling abilities, landcycling and typecycling included (CR 702.29a). */
    CYCLING,

    /** Unearth abilities, granted ones included (CR 702.84a). */
    UNEARTH;

    /** Whether {@code ability} is of this kind. */
    public boolean matches(ActivatedAbility ability) {
        return switch (this) {
            case ANY -> true;
            case POWER_UP -> ability.isPowerUpAbility();
            case BOAST -> ability.isBoast();
            case EXHAUST -> ability.isExhaustAbility();
            case NON_MANA -> !ability.isManaAbility();
            case EQUIP -> ability.isEquipAbility();
            case CYCLING -> ability.isCyclingAbility();
            case UNEARTH -> ability.isUnearthAbility();
        };
    }
}
