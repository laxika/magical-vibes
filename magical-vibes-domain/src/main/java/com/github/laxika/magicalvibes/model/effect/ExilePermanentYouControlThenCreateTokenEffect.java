package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Chooses and exiles one matching permanent the controller controls, then creates a token. */
public record ExilePermanentYouControlThenCreateTokenEffect(
        PermanentPredicate filter,
        CreateTokenEffect token
) implements CardEffect {
}
