package com.github.laxika.magicalvibes.model.effect;

/**
 * Return "that card" — the stack entry's triggering card, the card of the permanent that was put into a graveyard
 * from the battlefield, or the card a manifest dread put into a graveyard — from its owner's graveyard to its owner's
 * hand. Does nothing if the card has left that graveyard (or is a new object there, CR 400.7), e.g. it was a token.
 * Used by Enduring Renewal — "Whenever a creature is put into your graveyard from the battlefield, return it to your
 * hand." — and by Yomiji, Who Bars the Way — "Whenever a legendary permanent other than Yomiji is put into a
 * graveyard from the battlefield, return that card to its owner's hand." A card is put into its owner's graveyard,
 * so the graveyard that holds it names the hand.
 */
public record ReturnTriggeringCardToOwnerHandEffect() implements CardEffect {
}
