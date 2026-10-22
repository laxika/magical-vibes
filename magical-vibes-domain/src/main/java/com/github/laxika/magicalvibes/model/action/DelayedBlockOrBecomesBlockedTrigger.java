package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Delayed trigger: "whenever [permanent] blocks or becomes blocked by a creature this combat,
 * [effect]" (Goblin Flotilla). It fires once for each creature the permanent blocks or is blocked
 * by, with that creature as the effect's target. Its controller is the player who controlled the
 * ability that created it (CR 603.7e), even if the permanent later changes control. Cleared at end
 * of combat.
 */
public record DelayedBlockOrBecomesBlockedTrigger(UUID permanentId, UUID controllerId, Card sourceCard,
                                                  CardEffect effect)
        implements DelayedAction {
}
