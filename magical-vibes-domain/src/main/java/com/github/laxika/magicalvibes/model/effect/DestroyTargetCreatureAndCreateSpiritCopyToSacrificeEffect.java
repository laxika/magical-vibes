package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted creature (it can't be regenerated), then creates a black Spirit creature
 * token under the spell's controller whose power and toughness equal that creature's last-known
 * power and toughness. The token is sacrificed at the beginning of the next end step. The token is
 * created regardless of whether the destruction actually succeeds (e.g. indestructible targets).
 * Used by Broken Visage.
 */
public record DestroyTargetCreatureAndCreateSpiritCopyToSacrificeEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
