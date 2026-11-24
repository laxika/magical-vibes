package com.github.laxika.magicalvibes.model.effect;

/**
 * Static: "You may pay {0} rather than pay the [kind] cost [of the first [kind] ability you activate each turn]" for the
 * abilities of {@code kind} its controller activates: an alternative activation cost (CR 118.9, 602.2b) that replaces the
 * whole cost of that kind of ability. Read at activation by {@code CastingCostService.paysZeroRatherThanAbilityCost} from
 * the controller's active static effects, so a condition ("as long as you have an enduring story", "during each of your
 * turns", "max speed", "seven or more cards in hand") is a {@link ConditionalEffect} around it.
 *
 * @param kind          the abilities it applies to: {@link ActivatedAbilityKind#EQUIP}, {@link ActivatedAbilityKind#POWER_UP},
 *                      {@link ActivatedAbilityKind#CYCLING} or {@link ActivatedAbilityKind#UNEARTH}
 * @param firstEachTurn only for the first ability of that kind the player activates this turn, whether or not this
 *                      effect was there for the earlier one (Bruenor Battlehammer, Gavi, Nest Warden)
 */
public record PayZeroRatherThanAbilityCostEffect(ActivatedAbilityKind kind, boolean firstEachTurn) implements CardEffect {

    public PayZeroRatherThanAbilityCostEffect {
        switch (kind) {
            case EQUIP, POWER_UP, CYCLING, UNEARTH -> { }
            default -> throw new IllegalArgumentException("No per-turn activation record for " + kind + " abilities");
        }
    }

    /** "You may pay {0} rather than pay the [kind] cost of the first [kind] ability you activate each turn." */
    public static PayZeroRatherThanAbilityCostEffect firstEachTurn(ActivatedAbilityKind kind) {
        return new PayZeroRatherThanAbilityCostEffect(kind, true);
    }

    /** "You may pay {0} rather than pay [kind] costs." */
    public static PayZeroRatherThanAbilityCostEffect always(ActivatedAbilityKind kind) {
        return new PayZeroRatherThanAbilityCostEffect(kind, false);
    }
}
