package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;

import java.util.Objects;
import java.util.UUID;

/**
 * An object whose triggered abilities watch the game's events from where they function: a permanent on the
 * battlefield, under its controller, or an emblem in the command zone (CR 114.4), under its owner's control. The event
 * matchers read every watcher alike and hand its abilities to the stack door with the watcher as their source.
 *
 * @param card         the watcher's card: the permanent's, or the card the emblem was made of
 * @param permanent    the permanent, or {@code null} for an emblem
 * @param controllerId who controls the watcher's abilities
 * @param emblem       the emblem, or {@code null} for a permanent
 */
record Watcher(Card card, Permanent permanent, UUID controllerId, Emblem emblem) {

    Watcher {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(controllerId, "controllerId");
    }

    static Watcher of(Permanent permanent, UUID controllerId) {
        return new Watcher(permanent.getCard(), permanent, controllerId, null);
    }

    static Watcher of(Emblem emblem) {
        return new Watcher(emblem.sourceCard(), null, emblem.controllerId(), emblem);
    }

    /** The id its once-each-turn marks are kept under: the permanent's, or the emblem's card's. */
    UUID id() {
        return permanent != null ? permanent.getId() : card.getId();
    }

    /** The card a filter names as the watcher's own: the permanent's physical card, or the emblem's card. */
    UUID originalCardId() {
        return permanent != null ? permanent.getOriginalCard().getId() : card.getId();
    }

    /** The watcher as the permanent a filter reads it as; an emblem is read as a stand-in of its card. */
    Permanent asPermanent() {
        return permanent != null ? permanent : new Permanent(card);
    }

    /** The context an ability of the watcher judges its conditions in. */
    ConditionContext conditionContext() {
        return permanent != null ? ConditionContext.forPermanent(permanent, controllerId)
                : ConditionContext.forCard(card, controllerId);
    }
}
