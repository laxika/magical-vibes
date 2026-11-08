package com.github.laxika.magicalvibes.model.condition;

/**
 * Intervening-if for abilities that trigger from a graveyard ("... if this card is in your
 * graveyard, ..."): the source card object is still in a graveyard when the ability resolves.
 * Fails if the card left the graveyard in the meantime, so the ability does nothing (CR 603.4):
 * the stack entry records the graveyard incarnation the card had as the ability triggered
 * ({@code StackEntry.sourceCardGraveyardEntryVersion()}), and a card that left and came back is a
 * new object (CR 400.7). Used by Vengeful Pharaoh.
 */
public record SourceCardInGraveyard() implements Condition {

    @Override
    public String conditionName() {
        return "source in graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is no longer in a graveyard";
    }
}
