package com.github.laxika.magicalvibes.model.effect;

/**
 * A card outside the battlefield that gained an ability for a while (CR 611.2a): "it gains flashback until end of
 * turn", "you may cast it with jump-start". It is the payload of a floating effect on the card
 * ({@code FloatingContinuousEffect#onCard}), so it ends at its duration or as the card leaves its zone (CR 400.7).
 *
 * @param ability                what the card gained
 * @param cost                   the cost the grant fixes (flashback, unearth), or {@code null} for the card's mana cost
 * @param withoutPayingManaCost  flashback: the card is cast without paying its mana cost
 * @param despiteLostAbilities   flashback: granted while graveyard cards had already lost all abilities, so it
 *                               survives that loss (Snapcaster Mage under Yixlid Jailer)
 */
public record GrantedCardAbilityEffect(Ability ability, String cost, boolean withoutPayingManaCost,
                                       boolean despiteLostAbilities) implements CardEffect {

    /** What a card outside the battlefield can be granted for a while. */
    public enum Ability {
        FLASHBACK, JUMP_START, HARMONIZE, EMBALM, UNEARTH, WARP
    }

    /** A grant of {@code ability} at the card's mana cost. */
    public static GrantedCardAbilityEffect of(Ability ability) {
        return new GrantedCardAbilityEffect(ability, null, false, false);
    }

    /** A grant of {@code ability} at the fixed {@code cost}. */
    public static GrantedCardAbilityEffect of(Ability ability, String cost) {
        return new GrantedCardAbilityEffect(ability, cost, false, false);
    }
}
