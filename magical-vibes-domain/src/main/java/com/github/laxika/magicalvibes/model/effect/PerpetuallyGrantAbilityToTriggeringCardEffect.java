package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;

import java.util.Objects;

/**
 * "It perpetually gains "[ability]"" of the card the trigger event was about ("whenever a land enters under your
 * control, it perpetually gains ...", "whenever a creature card is put into your graveyard from your library or hand,
 * it perpetually gains ...", "whenever this or equipped creature dies, it perpetually gains ..."): the stack entry's
 * triggering card wherever it is now, else the ability's own card (digital only). The grant goes into the one perpetual
 * ability store ({@code GameData.grantPerpetualAbility}), any kind of ability; a repeated grant is its own instance.
 * It logs nothing.
 *
 * @param grantedAbility what the card gains
 */
public record PerpetuallyGrantAbilityToTriggeringCardEffect(Ability grantedAbility) implements CardEffect {

    public PerpetuallyGrantAbilityToTriggeringCardEffect {
        Objects.requireNonNull(grantedAbility, "grantedAbility");
    }
}
