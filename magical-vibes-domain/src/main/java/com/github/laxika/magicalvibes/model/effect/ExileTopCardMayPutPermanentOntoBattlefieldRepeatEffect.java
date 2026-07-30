package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top card of your library. If it's a permanent card, you may put it onto the
 * battlefield. If you do, repeat this process.
 *
 * <p>Each iteration exiles one card; a nonpermanent card ends the process and stays in exile.
 * For a permanent card the controller is prompted via
 * {@link com.github.laxika.magicalvibes.model.PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice}
 * — accepting puts it onto the battlefield and repeats, declining ends the process and leaves the
 * card in exile. Used by Primal Surge.
 */
public record ExileTopCardMayPutPermanentOntoBattlefieldRepeatEffect() implements CardEffect {
}
