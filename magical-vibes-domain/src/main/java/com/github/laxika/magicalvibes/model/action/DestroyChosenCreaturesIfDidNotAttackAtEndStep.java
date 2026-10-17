package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/**
 * Oracle en-Vec: at the beginning of the next end step, destroy each of the listed creatures that
 * didn't attack this turn, as a single delayed trigger. The trigger is controlled by the player who
 * controlled the ability that created it, and its source is that ability's source (CR 603.7e). A
 * null {@code controllerId} or {@code sourceCard} falls back to each destroyed creature's controller
 * and card.
 */
public record DestroyChosenCreaturesIfDidNotAttackAtEndStep(List<UUID> permanentIds, UUID controllerId,
        Card sourceCard) implements DelayedAction {
}
