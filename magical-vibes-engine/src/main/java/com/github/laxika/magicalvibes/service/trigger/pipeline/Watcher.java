package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;

import java.util.Objects;
import java.util.UUID;

/**
 * An object whose triggered abilities watch the game's events from where they function: a permanent on the
 * battlefield, under its controller; an emblem in the command zone (CR 114.4), under its owner's control; or a delayed
 * triggered ability a resolving spell or ability created (CR 603.7a), under the control of the player who controlled
 * that spell or ability (CR 603.7d, 603.7e). The event matchers read every watcher alike and hand its abilities to the
 * stack door with the watcher as their source.
 *
 * @param card         the watcher's card: the permanent's, the card the emblem was made of, or the card of the spell
 *                     or ability that created the delayed ability (its source, CR 603.7d, 603.7e)
 * @param permanent    the permanent, or {@code null} for an emblem or a delayed ability
 * @param controllerId who controls the watcher's abilities
 * @param emblem       the emblem, or {@code null}
 * @param delayed      the delayed triggered ability, or {@code null}
 */
record Watcher(Card card, Permanent permanent, UUID controllerId, Emblem emblem,
               TemporaryGlobalTriggeredAbility delayed) {

    Watcher {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(controllerId, "controllerId");
    }

    static Watcher of(Permanent permanent, UUID controllerId) {
        return new Watcher(permanent.getCard(), permanent, controllerId, null, null);
    }

    static Watcher of(Emblem emblem) {
        return new Watcher(emblem.sourceCard(), null, emblem.controllerId(), emblem, null);
    }

    static Watcher of(TemporaryGlobalTriggeredAbility delayed) {
        return new Watcher(delayed.sourceCard(), null, delayed.controllerId(), null, delayed);
    }

    /** The id its once-each-turn marks are kept under: the permanent's, or the emblem's or delayed ability's card's. */
    UUID id() {
        return permanent != null ? permanent.getId() : card.getId();
    }

    /** The card a filter names as the watcher's own: the permanent's physical card, or the emblem's card. */
    UUID originalCardId() {
        return permanent != null ? permanent.getOriginalCard().getId() : card.getId();
    }

    /**
     * The permanent a filter names as "this": the permanent itself, for a delayed ability the permanent whose ability
     * created it (its source, CR 603.7e: "a card exiled with this", Apple of Eden, Isu Relic), or {@code null}.
     */
    UUID sourcePermanentId() {
        if (permanent != null) {
            return permanent.getId();
        }
        return delayed != null ? delayed.sourcePermanentId() : null;
    }

    /** The watcher as the permanent a filter reads it as; an emblem or delayed ability as a stand-in of its card. */
    Permanent asPermanent() {
        return permanent != null ? permanent : new Permanent(card);
    }

    /** The context an ability of the watcher judges its conditions in. */
    ConditionContext conditionContext() {
        return permanent != null ? ConditionContext.forPermanent(permanent, controllerId)
                : ConditionContext.forCard(card, controllerId);
    }
}
