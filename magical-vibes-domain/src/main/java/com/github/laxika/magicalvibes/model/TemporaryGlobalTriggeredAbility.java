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
 * @param ability the ability, one effect in a legacy slot
 */
public record TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, TriggeredAbility ability,
                                              TargetFilter targetFilter,
                                              boolean untilEndOfNextTurn, boolean untilNextTurn,
                                              int registrationTurnNumber,
                                              UUID expirationPlayerId,
                                              UUID watchedPermanentId) {

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

    /** The effect of {@link #ability}. */
    public CardEffect effect() {
        return ability.effects().getFirst().effect();
    }
}
