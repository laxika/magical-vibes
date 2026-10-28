package com.github.laxika.magicalvibes.model.effect;

/**
 * Fatal Lore. An opponent chooses one — either the controller draws three cards, or the controller
 * destroys up to two creatures that opponent controls (they can't be regenerated) and that opponent
 * draws up to three cards. Per CR 700.2e the opponent chooses the mode while the spell is cast, through
 * the may-ability (accept/decline) system; the choice is recorded on the spell as {@code accepted}
 * (accept = draw three, decline = destroy mode) and carried out on resolution.
 *
 * @param accepted the mode the opponent chose, or {@code null} while no choice has been made
 */
public record FatalLoreEffect(Boolean accepted) implements CardEffect {

    public FatalLoreEffect() {
        this(null);
    }
}
