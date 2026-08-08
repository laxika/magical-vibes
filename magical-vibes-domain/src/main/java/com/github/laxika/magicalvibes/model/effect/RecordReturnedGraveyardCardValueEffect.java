package com.github.laxika.magicalvibes.model.effect;

/**
 * Graveyard-return glue effect. A preceding {@link ReturnCardFromGraveyardEffect} returns the
 * targeted graveyard card ({@code entry.targetId}, targetZone GRAVEYARD) to its owner's hand; this
 * effect records the requested characteristic of that card as the entry's event value so a
 * following {@code DealDamageToAnyTargetEffect(new EventValue())} deals it to the chosen any target.
 *
 * <p>Left unbound to any target group so the resolver keeps {@code entry.targetId} pointing at the
 * graveyard card (a bound effect would be remapped to the any target). The gate confirms the card is
 * now in the controller's hand rather than inferring the return from its type, so a graveyard target
 * that became illegal (exiled in response, CR 608.2b) records 0 and deals no damage.
 *
 * @param value which characteristic to record — see {@link ReturnedGraveyardCardValue}
 */
public record RecordReturnedGraveyardCardValueEffect(ReturnedGraveyardCardValue value) implements CardEffect {
}
