package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Static: "you have protection from [quality]". Printed on a permanent, it protects that permanent's controller ("you
 * have protection from each of your opponents", Absolute Virtue; "you have protection from the chosen card name",
 * Runed Halo with {@link ChooseCardNameOnEnterEffect}); floated by {@link GrantStaticEffectToPlayerEffect}, it
 * protects the player it is on. Only the player has it, never the permanent.
 *
 * <p>The player can't be targeted by spells or abilities from sources with the quality, can't be enchanted by Auras
 * with it, and damage from sources with it is prevented (CR 702.16b, 702.16c, 702.16e); the equipment, fortification
 * and blocking parts of protection don't apply to players. {@code GameQueryService.playerProtectionFrom} is the one
 * reader, beside the other player protections.
 *
 * @param from the quality the player has protection from
 */
public record PlayerHasProtectionEffect(Quality from) implements CardEffect {

    /** What the player has protection from. */
    public enum Quality {
        /**
         * Each of the player's opponents: every object an opponent controls, and every object an opponent owns that
         * no other player controls (CR 702.16k).
         */
        EACH_OPPONENT,
        /**
         * The card name chosen for the permanent with this ability, judged by the source's name (a face-down source
         * has none). A floated copy has no chosen name, so it protects from nothing.
         */
        CHOSEN_NAME
    }

    public PlayerHasProtectionEffect {
        Objects.requireNonNull(from, "from");
    }
}
