package com.github.laxika.magicalvibes.model.effect;

/**
 * Words of Wilding's activated ability: registers a one-shot replacement effect (CR 614.1a) of the
 * controller's next draw this turn with creating a 2/2 green Bear creature token. The resolution makes a
 * floating effect on the controller whose payload is this record with the set code of the source card.
 *
 * @param sourceSetCode the set code the token is created for; {@code null} on the card's own copy of the record
 */
public record RegisterNextDrawCreateBearReplacementEffect(String sourceSetCode) implements CardEffect {

    public RegisterNextDrawCreateBearReplacementEffect() {
        this(null);
    }
}
