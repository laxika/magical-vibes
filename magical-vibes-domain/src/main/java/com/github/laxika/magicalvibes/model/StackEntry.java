package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class StackEntry {

    private final StackEntryType entryType;
    private final Card card;
    private final UUID controllerId;
    private final String description;
    private final List<CardEffect> effectsToResolve;
    private final int xValue;
    @Setter private UUID targetId;
    private final UUID sourcePermanentId;
    private final Map<UUID, Integer> damageAssignments;
    private final Zone targetZone;
    private final List<UUID> targetCardIds;
    @Setter private TargetFilter targetFilter;
    @Setter private boolean copy;
    @Setter private boolean nonTargeting;
    @Setter private boolean returnToHandAfterResolving;
    @Setter private boolean castWithFlashback;
    @Setter private Zone sourceZone;
    @Setter private boolean kicked;
    /** Whether this spell was cast for its evoke (alternate) cost — carried to the entering permanent. */
    @Setter private boolean evoked;
    /** Whether this spell was cast for its prowl cost — carried to the entering permanent so its
     *  "if its prowl cost was paid" ETB trigger can gate on it (CR 702.75). */
    @Setter private boolean prowl;
    @Setter private Card damageSourceCard;
    @Setter private int stateTriggerEffectIndex = -1;
    @Setter private UUID attackedTargetId;
    /**
     * The integer payload of the event (or prior resolution step) behind this entry — life gained,
     * damage dealt, excess damage, etc. Snapshotted by the trigger collector that enqueues the entry
     * (parallel to {@link #xValue}, but for trigger-event data rather than cast-time data), or set by
     * an earlier effect on the same entry (e.g. excess damage from a damage effect). Read back by the
     * {@code EventValue} dynamic amount at resolution.
     */
    @Setter private int eventValue;
    /**
     * Last-known snapshot of the source permanent, set at activation time. Used to evaluate
     * source-relative amounts (e.g. counters on the source) per CR 608.2h last-known
     * information when the source left the battlefield before resolution (sacrifice costs).
     */
    @Setter private Permanent sourcePermanentSnapshot;
    private final List<UUID> targetIds;
    /**
     * Ids of permanents (tokens) created by effects earlier in <em>this</em> resolution. Populated
     * by the token-creation handlers and read back by a later effect on the same entry that acts on
     * "those tokens" — e.g. Gilt-Leaf Ambush grants deathtouch on a clash win to the tokens it just
     * created via {@code GrantScope.TOKENS_CREATED_THIS_RESOLUTION}.
     */
    private final List<UUID> createdPermanentIds = new ArrayList<>();

    /**
     * A card referenced by a stack entry is live game state shared with AI simulation copies —
     * freeze it so any later mutation of the Card object fails fast instead of leaking.
     */
    private static Card freezeCard(Card card) {
        if (card != null) {
            card.freeze();
        }
        return card;
    }

    // Creature spell constructor
    public StackEntry(Card card, UUID controllerId) {
        this.entryType = StackEntryType.CREATURE_SPELL;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = card.getName();
        this.effectsToResolve = List.of();
        this.xValue = 0;
        this.targetId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = List.of();
    }

    // Triggered ability constructor
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = List.of();
    }

    // General constructor with xValue (for sorcery spells)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, int xValue) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = List.of();
    }

    // Targeted or damage distribution spell constructor
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, int xValue, UUID targetId, Map<UUID, Integer> damageAssignments) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetId = targetId;
        this.sourcePermanentId = null;
        this.damageAssignments = damageAssignments != null ? damageAssignments : Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = List.of();
    }

    // Triggered ability with source permanent and xValue constructor (e.g. spell-cast self-boost by mana spent)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description,
                      List<CardEffect> effectsToResolve, int xValue, UUID sourcePermanentId) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetId = null;
        this.sourcePermanentId = sourcePermanentId;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = List.of();
    }

    // Triggered ability with source and target permanent constructor
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, UUID targetId, UUID sourcePermanentId) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetId = targetId;
        this.sourcePermanentId = sourcePermanentId;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = List.of();
    }

    // Zone-aware targeted ability constructor (e.g. target a card in graveyard)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, UUID targetId, Zone targetZone) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetId = targetId;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = targetZone;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = List.of();
    }

    // Spell copy constructor - preserves all fields from the original stack entry
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description,
                      List<CardEffect> effectsToResolve, int xValue, UUID targetId,
                      UUID sourcePermanentId, Map<UUID, Integer> damageAssignments,
                      Zone targetZone, List<UUID> targetCardIds, List<UUID> targetIds) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetId = targetId;
        this.sourcePermanentId = sourcePermanentId;
        this.damageAssignments = damageAssignments != null ? damageAssignments : Map.of();
        this.targetZone = targetZone;
        this.targetCardIds = targetCardIds != null ? targetCardIds : List.of();
        this.targetFilter = null;
        this.targetIds = targetIds != null ? targetIds : List.of();
    }

    // Multi-target triggered ability constructor (e.g. exile up to N cards from graveyards)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, List<UUID> targetCardIds) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = targetCardIds != null ? targetCardIds : List.of();
        this.targetFilter = null;
        this.targetIds = List.of();
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     * Card and CardEffect references are shared (immutable after construction).
     */
    public StackEntry(StackEntry source) {
        this.entryType = source.entryType;
        this.card = source.card;
        this.controllerId = source.controllerId;
        this.description = source.description;
        this.effectsToResolve = new ArrayList<>(source.effectsToResolve);
        this.xValue = source.xValue;
        this.targetId = source.targetId;
        this.sourcePermanentId = source.sourcePermanentId;
        this.damageAssignments = source.damageAssignments.isEmpty() ? Map.of() : new HashMap<>(source.damageAssignments);
        this.targetZone = source.targetZone;
        this.targetCardIds = source.targetCardIds.isEmpty() ? List.of() : new ArrayList<>(source.targetCardIds);
        this.targetFilter = source.targetFilter;
        this.copy = source.copy;
        this.nonTargeting = source.nonTargeting;
        this.returnToHandAfterResolving = source.returnToHandAfterResolving;
        this.castWithFlashback = source.castWithFlashback;
        this.sourceZone = source.sourceZone;
        this.kicked = source.kicked;
        this.evoked = source.evoked;
        this.prowl = source.prowl;
        this.damageSourceCard = source.damageSourceCard;
        this.stateTriggerEffectIndex = source.stateTriggerEffectIndex;
        this.attackedTargetId = source.attackedTargetId;
        this.eventValue = source.eventValue;
        this.sourcePermanentSnapshot = source.sourcePermanentSnapshot;
        this.targetIds = source.targetIds.isEmpty() ? List.of() : new ArrayList<>(source.targetIds);
    }

    // Multi-target triggered ability with source permanent constructor (e.g. "two target players exchange life totals")
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, UUID sourcePermanentId, List<UUID> targetIds) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetId = null;
        this.sourcePermanentId = sourcePermanentId;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = targetIds != null ? targetIds : List.of();
    }

    // Multi-target permanent spell constructor (e.g. "one or two target creatures")
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, int xValue, List<UUID> targetIds) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetIds = targetIds != null ? targetIds : List.of();
    }

    /**
     * Returns the card to use as the damage source for protection and prevention checks.
     * Normally this is the same as {@link #getCard()}, but for equipment-granted abilities
     * like Blazing Torch the damage source is the equipment, not the equipped creature.
     */
    public Card getEffectiveDamageSourceCard() {
        return damageSourceCard != null ? damageSourceCard : card;
    }

    /**
     * Returns the targets chosen for the given target group, resolved against this entry's
     * flat {@link #targetIds} list.
     *
     * <p>For spells, the flat list is sliced by the card's {@link SpellTarget} declarations in
     * order: the first group's chosen targets come first, then the next group's, and so on —
     * matching how targets are selected position-by-position against
     * {@link Card#getMultiTargetFilters()} and validated by the target legality service. Each
     * group consumes up to its {@code maxTargets} of the remaining ids, so a group with a
     * variable target count ("up to N") must be the last declared group — with a variable-count
     * group in any earlier position the flat wire format would be ambiguous (no such card
     * exists; the DSL assumes declaration-order filling).</p>
     *
     * <p>When the card declares no spell targets (e.g. activated abilities with their own
     * multi-target filter list), the flat list is treated as positional: group {@code g} is
     * {@code targetIds.get(g)}.</p>
     *
     * <p>Aura entries are the exception: the enchant target (group 0) is stored separately in
     * {@link #targetId} — both on the spell entry (see {@code SpellCastingService}'s aura split)
     * and on the aura's ETB trigger entry, which inherits that shape — so the flat list holds
     * only the later groups' targets and slicing starts at group 1.</p>
     */
    public List<UUID> targetsForGroup(int group) {
        List<SpellTarget> groups = card == null ? List.of() : card.getSpellTargets();
        if (groups.isEmpty()) {
            return group >= 0 && group < targetIds.size() ? List.of(targetIds.get(group)) : List.of();
        }
        int firstFlatGroup = 0;
        if (card.isAura() && targetId != null) {
            if (group == 0) {
                return List.of(targetId);
            }
            firstFlatGroup = 1;
        }
        int consumed = 0;
        for (SpellTarget g : groups) {
            if (g.getIndex() < firstFlatGroup) {
                continue;
            }
            int size = Math.min(Math.max(g.getMaxTargets(), 0), targetIds.size() - consumed);
            if (g.getIndex() == group) {
                return List.copyOf(targetIds.subList(consumed, consumed + size));
            }
            consumed += size;
        }
        return List.of();
    }

    /**
     * Returns the targets the given effect applies to.
     *
     * <p>An effect bound to a target group via {@code target(...).addEffect(...)} applies to
     * all targets chosen for its group (see {@link #targetsForGroup}). An unbound effect keeps
     * the legacy semantics: the whole flat {@link #targetIds} list. When the entry was cast
     * through the single-target path ({@link #targetId} set, flat list empty), a bound effect
     * resolves against that lone target.</p>
     */
    public List<UUID> targetsForEffect(CardEffect effect) {
        int group = card == null ? -1 : card.getEffectTargetIndex(effect);
        if (group < 0) {
            return targetIds;
        }
        if (targetIds.isEmpty()) {
            // On an aura the lone targetId is the enchant target (group 0), never a later
            // group's target — an effect bound to a later group simply has no target chosen.
            if (card.isAura() && group != 0) {
                return List.of();
            }
            return targetId != null ? List.of(targetId) : List.of();
        }
        return targetsForGroup(group);
    }

    public boolean isSingleTarget() {
        return targetId != null && targetIds.isEmpty() && targetCardIds.isEmpty();
    }

    public boolean hasAnyTarget() {
        return targetId != null || !targetIds.isEmpty() || !targetCardIds.isEmpty();
    }
}
