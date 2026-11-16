package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.UUID;

/**
 * A global triggered ability registered by a resolving effect.
 * The source card is retained because the spell that registered the ability is no longer on the
 * battlefield when the trigger fires. Duration flags distinguish ordinary end-of-turn triggers,
 * end-of-next-turn triggers, and triggers that expire at the beginning of the controller's next
 * turn.
 * The optional watched permanent identifies a particular object for a delayed event trigger.
 *
 * @param ability            the ability: every effect of it goes on the stack as one object (CR 603.3)
 * @param sourcePermanentId  the permanent that was the source of the ability that registered this one, which is this
 *                           one's source too (CR 603.7e), recorded whatever the effects are: the "this" of "whenever you
 *                           cast a spell or play a land from among cards exiled with this" (Apple of Eden, Isu Relic),
 *                           judged by the trigger's {@code exiledWithThis()} filter; {@code null} when it had none
 * @param watchedPlayerId    the player the registering ability named (its target, or the defending player of an attack
 *                           trigger), recorded whatever the effects are: the "they" of "until the end of defending
 *                           player's next turn, that player gets two rad counters whenever they cast a spell" (Nuka-Nuke
 *                           Launcher), judged by the trigger's {@code StackEntryControlledByWatchedPlayerPredicate}
 *                           filter; {@code null} when it named none
 * @param linkedExiledCardId the card the registering ability's source had exiled with its linked ability as the
 *                           ability registered (CR 603.7c), recorded whatever the effects are: Surge to Victory's "the
 *                           exiled card", which the collector puts on the entry as its linked exiled card, so casting
 *                           the same spell again and exiling another card can't redirect it; {@code null} when it had
 *                           exiled none
 * @param untilTriggered     whether it has no stated duration: it triggers only the next time its trigger event occurs
 *                           and lasts until then (CR 603.7b), so no turn's end removes it; "when that creature leaves
 *                           the battlefield or becomes untapped" (Merieke Ri Berit). One about a permanent that left the
 *                           battlefield without triggering it never can: the permanent is gone (CR 400.7)
 */
public record TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                              TargetFilter targetFilter,
                                              boolean untilEndOfNextTurn, boolean untilNextTurn,
                                              int registrationTurnNumber,
                                              UUID expirationPlayerId,
                                              UUID watchedPermanentId,
                                              UUID sourcePermanentId,
                                              UUID watchedPlayerId,
                                              UUID linkedExiledCardId,
                                              boolean untilTriggered) {

    /** {@code ability} with a stated duration (CR 603.7b). */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber, UUID expirationPlayerId,
                                           UUID watchedPermanentId, UUID sourcePermanentId, UUID watchedPlayerId,
                                           UUID linkedExiledCardId) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                linkedExiledCardId, false);
    }

    /**
     * {@code ability} of the delayed triggered ability a resolving spell or ability with the source {@code sourceCard}
     * and {@code sourcePermanentId} (or none) created, controlled by {@code controllerId}, about the permanent
     * {@code watchedPermanentId} ("when that creature leaves the battlefield this turn"): until end of turn when
     * {@code thisTurn}, else until it triggers (CR 603.7b).
     */
    public static TemporaryGlobalTriggeredAbility about(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                                        UUID watchedPermanentId, UUID sourcePermanentId,
                                                        boolean thisTurn) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                watchedPermanentId, sourcePermanentId, null, null, !thisTurn);
    }

    /**
     * {@code ability} of the delayed triggered ability a resolving spell or ability with the source {@code sourceCard}
     * and {@code sourcePermanentId} (or none) created until end of turn, controlled by {@code controllerId}, that
     * watches no particular object ("whenever a creature dealt damage this way dies this turn").
     */
    public static TemporaryGlobalTriggeredAbility thisTurn(UUID controllerId, Card sourceCard,
                                                           TriggeredAbility ability, UUID sourcePermanentId) {
        return new TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability, null, false, false, -1, null,
                null, sourcePermanentId, null, null, false);
    }

    /** Whether a turn's end removes it: it has a stated duration that ends then (CR 603.7b, 514.2). */
    public boolean endsThisTurn() {
        return !untilTriggered && !untilEndOfNextTurn && !untilNextTurn;
    }

    /** {@code ability} with no linked exiled card. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter, boolean untilEndOfNextTurn,
                                           boolean untilNextTurn, int registrationTurnNumber,
                                           UUID expirationPlayerId, UUID watchedPermanentId,
                                           UUID sourcePermanentId, UUID watchedPlayerId) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, sourcePermanentId, watchedPlayerId,
                null);
    }

    /** {@code ability} with no registering source permanent and no watched player. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                           TargetFilter targetFilter, boolean untilEndOfNextTurn,
                                           boolean untilNextTurn, int registrationTurnNumber,
                                           UUID expirationPlayerId, UUID watchedPermanentId) {
        this(controllerId, sourceCard, ability, targetFilter, untilEndOfNextTurn, untilNextTurn,
                registrationTurnNumber, expirationPlayerId, watchedPermanentId, null, null);
    }

    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, EffectSlot slot,
                                           CardEffect effect, TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber, UUID expirationPlayerId,
                                           UUID watchedPermanentId) {
        this(controllerId, sourceCard, LegacySlots.triggered(slot, effect), targetFilter, untilEndOfNextTurn,
                untilNextTurn, registrationTurnNumber, expirationPlayerId, watchedPermanentId);
    }

    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, EffectSlot slot,
                                           CardEffect effect, TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber, UUID expirationPlayerId) {
        this(controllerId, sourceCard, slot, effect, targetFilter, untilEndOfNextTurn,
                untilNextTurn, registrationTurnNumber, expirationPlayerId, null);
    }

    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, EffectSlot slot,
                                           CardEffect effect) {
        this(controllerId, sourceCard, slot, effect, null, false, false, -1, null);
    }

    /** {@code ability} until end of turn, with no target filter and no watched permanent. */
    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability) {
        this(controllerId, sourceCard, ability, null, false, false, -1, null, null);
    }

    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, EffectSlot slot,
                                           CardEffect effect, TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, int registrationTurnNumber) {
        this(controllerId, sourceCard, slot, effect, targetFilter, untilEndOfNextTurn, false,
                registrationTurnNumber, null);
    }

    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, EffectSlot slot,
                                           CardEffect effect, TargetFilter targetFilter,
                                           boolean untilEndOfNextTurn, boolean untilNextTurn,
                                           int registrationTurnNumber) {
        this(controllerId, sourceCard, slot, effect, targetFilter, untilEndOfNextTurn,
                untilNextTurn, registrationTurnNumber, null);
    }

    /** The legacy slot whose collectors decide when {@link #ability} triggers. */
    public EffectSlot slot() {
        return LegacySlots.slotOf(ability);
    }
}
