package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Create a [token]. It gains '… return the exiled card to the battlefield'" after a payment or step that exiled a card
 * this resolution (Timothar, Baron of Bats: "you may pay {1} and exile it. If you do, create a 1/1 black Bat …"): the
 * token is created and the cards of the resolution's {@code StepKind.EXILE} step are linked to it alone, so its
 * ability's {@code Subjects.exiledWithSource()} is that card (the ruling: "refers only to the card you exile during
 * the resolution of the ability that created that token"). "It gains" is a {@code GrantEffectToTargetEffect} over
 * {@code Subjects.createdThisResolution()} after it, so a copy of the token doesn't have the ability (ruling).
 *
 * @param token the token to create
 */
public record CreateTokenLinkedToCardsExiledThisWayEffect(CreateTokenEffect token) implements CardEffect {

    public CreateTokenLinkedToCardsExiledThisWayEffect {
        Objects.requireNonNull(token, "token");
    }
}
