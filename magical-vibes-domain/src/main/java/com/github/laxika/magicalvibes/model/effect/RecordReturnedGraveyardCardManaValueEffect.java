package com.github.laxika.magicalvibes.model.effect;

/**
 * Vengeful Rebirth glue effect. A preceding {@link ReturnCardFromGraveyardEffect} returns the
 * targeted graveyard card ({@code entry.targetId}, targetZone GRAVEYARD) to its owner's hand; this
 * effect records that card's mana value as the entry's event value so the following
 * {@code DealDamageToAnyTargetEffect(new EventValue())} deals it to the chosen any target — but only
 * when a <b>nonland</b> card was actually returned to the controller's hand this way ("If you return
 * a nonland card to your hand this way").
 *
 * <p>Left unbound to any target group so the resolver keeps {@code entry.targetId} pointing at the
 * graveyard card (a bound effect would be remapped to the any target). The gate confirms the card is
 * now in hand rather than inferring return from its type, so a graveyard target that became illegal
 * (exiled in response, CR 608.2b) records 0 and deals no damage. Records 0 for a returned land too.
 */
public record RecordReturnedGraveyardCardManaValueEffect() implements CardEffect {
}
