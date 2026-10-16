package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TurnStep;

/**
 * Schedules a delayed trigger that returns every card currently exiled with the source to the battlefield
 * under its owner's control at the beginning of the next {@code returnStep} (Lae'zel's Acrobatics). Unlike
 * {@link FlickerEffect}, it does not exile anything itself: the cards must already be source-tracked in exile.
 */
public record ReturnAllCardsExiledWithSourceAtNextStepEffect(TurnStep returnStep) implements CardEffect {
}
