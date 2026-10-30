package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * The "repeat this process" step of Primal Surge that is waiting for the prompt opened by the permanent
 * just put onto the battlefield (an as-enters choice or an Aura's host choice) to be answered, so the next
 * card is not exiled until that permanent has finished entering.
 *
 * @param controllerId the player whose library is exiled from
 * @param sourceName   name of the source card, for prompts and the log
 */
public record PendingExileTopCardRepeat(UUID controllerId, String sourceName) {
}
