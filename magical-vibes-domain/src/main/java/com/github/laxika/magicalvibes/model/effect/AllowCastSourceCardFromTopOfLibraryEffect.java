package com.github.laxika.magicalvibes.model.effect;

/** Static permission for this card to be cast while it is the top card of its owner's library. */
public record AllowCastSourceCardFromTopOfLibraryEffect()
        implements TopLibraryCardCastPermissionEffect {
}
