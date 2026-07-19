package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of creatures the source permanent devoured (sacrificed to its devour ability) as it
 * entered the battlefield. Reads {@code Permanent.getDevouredCreatures().size()} on the stack entry's
 * source permanent — "target player discards a card for each creature it devoured" (Tar Fiend).
 * See {@link DevouredCreaturesOfSubtype} for the by-subtype variant (Voracious Dragon).
 */
public record CreaturesDevoured() implements DynamicAmount {
}
