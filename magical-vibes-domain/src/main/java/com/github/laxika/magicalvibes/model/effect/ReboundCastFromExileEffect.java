package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers the delayed ability's exiled source card for a free cast from exile (rebound). A spell cast this way is
 * exiled instead of going to a graveyard when {@code exileInsteadOfGraveyard}.
 */
public record ReboundCastFromExileEffect(boolean exileInsteadOfGraveyard) implements CardEffect {

    /** Rebound as the keyword has it: the spell cast goes to its owner's graveyard as it would. */
    public ReboundCastFromExileEffect() {
        this(false);
    }
}
