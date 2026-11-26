package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the source card from its owner's graveyard to the battlefield under that owner's control, optionally tapped,
 * for the two parts {@link PutCardsOntoBattlefieldEffect} over {@code Subjects.sourceObjectCard()} has no component for.
 * Every other "return this card / it to the battlefield" is that put step.
 *
 * <ul>
 *   <li>Runeblade Raiser's "It perpetually loses this ability": the triggered ability stops triggering for this card's
 *       identity ({@code perpetuallyLosesThisAbility}).</li>
 *   <li>Bail Out's granted "return it to the battlefield tapped under its owner's control. It deals 1 damage to each
 *       opponent": the later steps use the returned permanent as their source ({@code useReturnedPermanentAsSource});
 *       when it doesn't return they keep the creature as it last existed, which still deals the damage (CR 608.2h).</li>
 * </ul>
 *
 * <p>Does nothing if the card is no longer the graveyard object the ability refers to (CR 400.7) or can't enter from a
 * graveyard (Grafdigger's Cage).
 *
 * @param tapped                       {@code true} to have it enter the battlefield tapped
 * @param perpetuallyLosesThisAbility  {@code true} to make this triggered ability stop triggering for this card identity
 * @param useReturnedPermanentAsSource whether subsequent effects use the returned permanent as their source
 */
public record ReturnSourceCardFromGraveyardToBattlefieldEffect(boolean tapped, boolean perpetuallyLosesThisAbility,
                                                               boolean useReturnedPermanentAsSource)
        implements CardEffect {
}
