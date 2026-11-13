package com.github.laxika.magicalvibes.model.effect;

/**
 * A player takes the initiative and ventures into the Undercity: the effect's controller ("you take the initiative"),
 * or, with {@code thatPlayer}, the player its ability names without targeting ("the controller of those creatures takes
 * the initiative", the initiative's own combat damage ability, CR 727.2).
 *
 * @param thatPlayer whether the player the ability names takes the initiative, not its controller
 */
public record TakeInitiativeEffect(boolean thatPlayer) implements CardEffect {

    /** "You take the initiative." */
    public TakeInitiativeEffect() {
        this(false);
    }
}
