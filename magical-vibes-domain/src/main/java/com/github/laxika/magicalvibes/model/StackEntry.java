package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class StackEntry {

    private final StackEntryType entryType;
    private final Card card;
    private final UUID controllerId;
    /** The player whose upkeep caused this entry's each-upkeep trigger, when applicable. */
    @Setter private UUID activePlayerId;
    private final String description;
    private List<CardEffect> effectsToResolve;
    private final int xValue;
    /** The ETB mode selected while casting a modal permanent, when it differs from the paid X. */
    @Setter private Integer etbMode;
    @Setter private UUID targetId;
    private final UUID sourcePermanentId;
    private final Map<UUID, Integer> damageAssignments;
    /** Colored mana spent to activate this ability, snapshotted so later activations cannot overwrite it. */
    @Setter private Map<ManaColor, Integer> activationManaSpent = Map.of();
    private final Zone targetZone;
    @Setter private List<UUID> targetCardIds;
    @Setter private TargetFilter targetFilter;
    @Setter private boolean copy;
    @Setter private boolean nonTargeting;
    @Setter private boolean returnToHandAfterResolving;
    /** When set, the resolved spell card is put into its owner's library at this 0-based position from the
     *  top instead of going to the graveyard (Approach of the Second Sun's "seventh from the top" = 6). */
    @Setter private Integer putIntoLibraryPositionAfterResolving;
    @Setter private boolean castWithFlashback;
    /**
     * Whether a replacement effect applies to this spell: "if that spell would be put into a graveyard,
     * exile it instead" (The Dawning Archaic, Chancellor of the Spires). Unlike
     * {@link #castWithFlashback} this only replaces the graveyard disposition, so return-to-hand /
     * into-library dispositions still win.
     */
    @Setter private boolean exileInsteadOfGraveyard;
    /** Whether this spell was cast via Disturb (CR 702.146) — enters transformed; exile on leave-to-GY. */
    @Setter private boolean castWithDisturb;
    /**
     * Whether this spell was cast transformed without paying its mana cost after a Siege battle
     * was defeated. Enters as the back face (like Disturb) but uses normal spell disposition on fizzle.
     */
    @Setter private boolean castTransformed;
    /** Whether a creature spell resolves as a face-down 2/2 from a morph cast. */
    @Setter private boolean castFaceDown;
    @Setter private Zone sourceZone;
    /**
     * Overrides the card's disposition owner when this spell is controlled by someone other than its
     * owner (e.g. cast from an opponent's hand via Sen Triplets). Null for the overwhelming majority of
     * casts, where owner == controller; {@link #getOwnerId()} then falls back to {@link #controllerId}.
     * Read by the graveyard/hand/library disposition paths and the permanent-spell entry ownership stamp.
     */
    @Setter private UUID ownerIdOverride;
    @Setter private boolean kicked;
    /**
     * Whether this spell's buyback cost was paid (CR 702.27). Stamped by
     * {@code SpellCastingService} when the caster announces buyback; read at resolution by
     * {@code BuybackPaid} conditions ("if buyback was paid, put this card into its owner's
     * hand as it resolves"). Buyback only applies to instants and sorceries, so this flag
     * only ever rides a spell entry.
     */
    @Setter private boolean buyback;
    /** Whether this spell's put-counter additional cost was paid. */
    @Setter private boolean putCounterCostPaid;
    /**
     * The individual mana payments the caster chose for this spell's
     * {@link com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost}, one entry
     * per repetition ("you may pay {1}{R} and/or {1}{G} any number of times"). Snapshotted at cast
     * time and read back at resolution by
     * {@link com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount}.
     */
    @Setter private List<String> repeatedAdditionalCosts = List.of();
    /**
     * Whether this spell was cast at a time a sorcery couldn't have been cast. Only stamped on hand
     * casts, and only read by the Mirage flash clause
     * ({@code FlashCastWithCleanupSacrificeEffect}), which flags the entering permanent for
     * sacrifice at the next cleanup step when it is set.
     */
    @Setter private boolean castWhenSorceryCouldNotBeCast;
    /** Whether this spell was cast for its evoke (alternate) cost — carried to the entering permanent. */
    @Setter private boolean evoked;
    private Card bestowOriginalCard;
    /** Whether this spell was cast for its prowl cost — carried to the entering permanent so its
     *  "if its prowl cost was paid" ETB trigger can gate on it (CR 702.75). */
    @Setter private boolean prowl;
    /** Whether this spell was cast for its overload cost (CR 702.96a): every "target" in its text
     *  reads "each", and per CR 702.96b the spell has no targets at all. */
    @Setter private boolean overloaded;
    /** Card exiled as an additional behold cost, pending the permanent spell entering. */
    @Setter private Card beheldCard;
    @Setter private UUID beheldCardOwnerId;
    @Setter private CardSubtype beholdChosenSubtype;
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
     * The per-permanent player payload behind this entry — one entry per permanent involved in the
     * event, holding that permanent's controller. Stamped by
     * {@code DestroyAllPermanentsEffectHandler} onto a rider entry with the controller of every
     * permanent actually destroyed, so "for each permanent destroyed this way, … that permanent's
     * controller …" riders can act once per permanent rather than once per player (Stench of Evil).
     * Duplicates are meaningful: a player who lost three lands appears three times.
     */
    @Setter private List<UUID> eventPlayerIds = List.of();
    /**
     * The per-permanent mana value payload behind this entry, positionally aligned with
     * {@link #eventPlayerIds}. Stamped by {@code DestroyAllPermanentsEffectHandler} with the
     * last-known mana value of every permanent actually destroyed (CR 608.2h), so
     * "the controller of each of those permanents gains life equal to its mana value" riders can read
     * a per-permanent value instead of the aggregate {@link #eventValue} count (Seeds of Innocence).
     */
    @Setter private List<Integer> eventManaValues = List.of();
    /**
     * Last-known snapshot of the source permanent, set at activation time. Used to evaluate
     * source-relative amounts (e.g. counters on the source) per CR 608.2h last-known
     * information when the source left the battlefield before resolution (sacrifice costs).
     */
    @Setter private Permanent sourcePermanentSnapshot;
    /**
     * The permanent chosen while activating an ability or resolving a library selection. Read back
     * at resolution by the {@code ChosenPermanentPower} dynamic amount so an effect can scale to
     * that permanent's power as the ability resolves.
     */
    @Setter private UUID chosenPermanentId;
    /**
     * Last-known card id of the event that produced this triggered ability, when an effect needs to
     * act on "that card" rather than a chosen target — e.g. the creature that died for Seraph's
     * {@code ON_DAMAGED_CREATURE_DIES} return. Not a target: it is never validated or fizzled.
     */
    @Setter private UUID triggeringCardId;
    /** Card id of the permanent sacrificed as an additional cost to cast this spell, when one was paid. */
    @Setter private UUID sacrificedCardId;
    /**
     * Id of the permanent whose event produced this triggered ability, when an effect needs to act on
     * "it" rather than a chosen target — e.g. the permanent that became tapped for Freyalise's Winds'
     * {@code ON_ALLY_PERMANENT_BECOMES_TAPPED} / {@code ON_OPPONENT_PERMANENT_BECOMES_TAPPED} counter.
     * Not a target: it is never validated or fizzled.
     */
    @Setter private UUID triggeringPermanentId;
    /** Controller of the triggering permanent when its non-targeting reference was captured. */
    @Setter private UUID triggeringPermanentControllerId;
    /** Power and toughness of the entering permanent when an evolve trigger was created. */
    @Setter private Integer triggeringPermanentPowerAtTrigger;
    @Setter private Integer triggeringPermanentToughnessAtTrigger;
    private List<UUID> targetIds;
    /**
     * Whether {@link #targetIds} was derived from the controller-announced amount assignments
     * (divided damage / prevention / counter distribution) rather than from the card's declared
     * target groups. When it is, the card's target groups describe the separate primary target
     * carried on {@link #targetId} — Fiery Justice's "target opponent gains 5 life" next to its
     * 5 divided damage — so group slicing and per-position filters must not be applied to the
     * assignment positions.
     */
    private boolean targetIdsFromAssignments;
    /**
     * How many targets each declared target group actually contributed to the flat {@link #targetIds}
     * list, in group order. Only set by the slot-by-slot trigger walker, which lets a controller
     * decline an optional ("up to N") group — there the default assumption that every group consumed
     * its full {@code maxTargets} would shift the later groups' slices onto the wrong targets. Empty
     * means "unknown", which keeps the positional slicing every ordinary multi-target spell uses.
     */
    @Setter private List<Integer> targetGroupSizes = List.of();
    /**
     * Flat target positions that became illegal while this entry was resolving. Keeping positions
     * instead of removing IDs preserves target-group boundaries when an earlier target becomes
     * illegal (CR 608.2b).
     */
    @Getter(AccessLevel.NONE)
    private final Set<Integer> illegalTargetIndices = new HashSet<>();
    /**
     * Keywords the resolving permanent enters with as a <em>granted</em> keyword rather than a
     * printed one (Choreographed Sparks: "the copy gains haste"). Kept off the {@link Card} so the
     * UI can tell granted keywords from printed ones; drained into the entering
     * {@code Permanent.grantedKeywords} by {@code StackResolutionService}.
     */
    private final Set<Keyword> grantedKeywordsOnEntry = EnumSet.noneOf(Keyword.class);
    /**
     * Bloodthirst granted to this creature spell while it is on the stack (Bloodlord of Vaasgoth:
     * "it gains bloodthirst 3"). Per CR 702.54c each instance of bloodthirst applies separately, so
     * repeated grants accumulate. Stamped onto the entering {@code Permanent.grantedBloodthirst} by
     * {@code StackResolutionService} and turned into +1/+1 counters by the as-enters replacement.
     */
    @Setter private int grantedBloodthirst;
    /**
     * Ids of permanents (tokens) created by effects earlier in <em>this</em> resolution. Populated
     * by the token-creation handlers and read back by a later effect on the same entry that acts on
     * "those tokens" — e.g. Gilt-Leaf Ambush grants deathtouch on a clash win to the tokens it just
     * created via {@code GrantScope.TOKENS_CREATED_THIS_RESOLUTION}.
     */
    private final List<UUID> createdPermanentIds = new ArrayList<>();

    /**
     * Players this entry has actually dealt damage to while resolving, in order. Written by the damage
     * handlers and read back by a later effect on the same entry that acts on "each player dealt damage
     * this way" — e.g. Chandra, Roaring Flame's ultimate hands an emblem only to the opponents whose
     * damage was not fully prevented.
     */
    private final List<UUID> playersDealtDamageThisResolution = new ArrayList<>();

    /** Records that this entry dealt damage to {@code playerId}; duplicates are ignored. */
    public void recordPlayerDealtDamage(UUID playerId) {
        if (playerId != null && !playersDealtDamageThisResolution.contains(playerId)) {
            playersDealtDamageThisResolution.add(playerId);
        }
    }

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
        this.targetIds = assignmentTargetIds(this.damageAssignments);
        this.targetIdsFromAssignments = !this.targetIds.isEmpty();
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
        boolean explicitTargetIds = targetIds != null && !targetIds.isEmpty();
        this.targetIds = explicitTargetIds ? targetIds : assignmentTargetIds(this.damageAssignments);
        this.targetIdsFromAssignments = !explicitTargetIds && !this.targetIds.isEmpty();
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
        this.activePlayerId = source.activePlayerId;
        this.description = source.description;
        this.effectsToResolve = new ArrayList<>(source.effectsToResolve);
        this.xValue = source.xValue;
        this.etbMode = source.etbMode;
        this.targetId = source.targetId;
        this.sourcePermanentId = source.sourcePermanentId;
        this.damageAssignments = source.damageAssignments.isEmpty() ? Map.of() : new HashMap<>(source.damageAssignments);
        this.activationManaSpent = source.activationManaSpent.isEmpty() ? Map.of() : new HashMap<>(source.activationManaSpent);
        this.targetZone = source.targetZone;
        this.targetCardIds = source.targetCardIds.isEmpty() ? List.of() : new ArrayList<>(source.targetCardIds);
        this.targetFilter = source.targetFilter;
        this.copy = source.copy;
        this.nonTargeting = source.nonTargeting;
        this.returnToHandAfterResolving = source.returnToHandAfterResolving;
        this.putIntoLibraryPositionAfterResolving = source.putIntoLibraryPositionAfterResolving;
        this.castWithFlashback = source.castWithFlashback;
        this.exileInsteadOfGraveyard = source.exileInsteadOfGraveyard;
        this.castWithDisturb = source.castWithDisturb;
        this.castTransformed = source.castTransformed;
        this.castFaceDown = source.castFaceDown;
        this.sourceZone = source.sourceZone;
        this.ownerIdOverride = source.ownerIdOverride;
        this.kicked = source.kicked;
        this.buyback = source.buyback;
        this.putCounterCostPaid = source.putCounterCostPaid;
        this.repeatedAdditionalCosts = source.repeatedAdditionalCosts.isEmpty()
                ? List.of() : new ArrayList<>(source.repeatedAdditionalCosts);
        this.castWhenSorceryCouldNotBeCast = source.castWhenSorceryCouldNotBeCast;
        this.evoked = source.evoked;
        this.bestowOriginalCard = source.bestowOriginalCard;
        this.prowl = source.prowl;
        this.overloaded = source.overloaded;
        this.beheldCard = source.beheldCard;
        this.beheldCardOwnerId = source.beheldCardOwnerId;
        this.beholdChosenSubtype = source.beholdChosenSubtype;
        this.damageSourceCard = source.damageSourceCard;
        this.stateTriggerEffectIndex = source.stateTriggerEffectIndex;
        this.attackedTargetId = source.attackedTargetId;
        this.eventValue = source.eventValue;
        this.eventPlayerIds = source.eventPlayerIds.isEmpty() ? List.of() : new ArrayList<>(source.eventPlayerIds);
        this.eventManaValues = source.eventManaValues.isEmpty() ? List.of() : new ArrayList<>(source.eventManaValues);
        this.sourcePermanentSnapshot = source.sourcePermanentSnapshot;
        this.chosenPermanentId = source.chosenPermanentId;
        this.triggeringCardId = source.triggeringCardId;
        this.sacrificedCardId = source.sacrificedCardId;
        this.triggeringPermanentId = source.triggeringPermanentId;
        this.triggeringPermanentControllerId = source.triggeringPermanentControllerId;
        this.triggeringPermanentPowerAtTrigger = source.triggeringPermanentPowerAtTrigger;
        this.triggeringPermanentToughnessAtTrigger = source.triggeringPermanentToughnessAtTrigger;
        this.targetIds = source.targetIds.isEmpty() ? List.of() : new ArrayList<>(source.targetIds);
        this.targetIdsFromAssignments = source.targetIdsFromAssignments;
        this.illegalTargetIndices.addAll(source.illegalTargetIndices);
        this.grantedKeywordsOnEntry.addAll(source.grantedKeywordsOnEntry);
        this.grantedBloodthirst = source.grantedBloodthirst;
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

    /** Splices a SequenceEffect's steps into the effect list after the given position (resolution-time expansion). */
    public void insertEffectsToResolve(int index, List<CardEffect> steps) {
        List<CardEffect> updated = new ArrayList<>(effectsToResolve);
        updated.addAll(index, steps);
        effectsToResolve = updated;
    }

    public void replaceEffectToResolve(int index, CardEffect effect) {
        List<CardEffect> updated = new ArrayList<>(effectsToResolve);
        updated.set(index, effect);
        effectsToResolve = updated;
    }

    /**
     * Returns the card to use as the damage source for protection and prevention checks.
     * Normally this is the same as {@link #getCard()}, but for equipment-granted abilities
     * like Blazing Torch the damage source is the equipment, not the equipped creature.
     */
    public Card getEffectiveDamageSourceCard() {
        return damageSourceCard != null ? damageSourceCard : card;
    }

    public void setBestowOriginalCard(Card bestowOriginalCard) {
        this.bestowOriginalCard = freezeCard(bestowOriginalCard);
    }

    public Card getPhysicalCard() {
        return bestowOriginalCard != null ? bestowOriginalCard : card;
    }

    /**
     * The player to whom this spell's card belongs for disposition purposes (graveyard/hand/library on
     * leaving the stack). Defaults to the controller — correct for every normal cast, where owner ==
     * controller — unless an {@link #ownerIdOverride} was set for a control-diverged cast (Sen Triplets).
     */
    public UUID getOwnerId() {
        return ownerIdOverride != null ? ownerIdOverride : controllerId;
    }

    /**
     * Returns only targets that are still legal for resolution. Before resolution-time legality is
     * checked this is the complete target list, preserving existing cast/trigger inspection behavior.
     */
    public List<UUID> getTargetIds() {
        if (illegalTargetIndices.isEmpty()) {
            return targetIds;
        }
        List<UUID> legalTargets = new ArrayList<>(targetIds.size() - illegalTargetIndices.size());
        for (int i = 0; i < targetIds.size(); i++) {
            if (!illegalTargetIndices.contains(i)) {
                legalTargets.add(targetIds.get(i));
            }
        }
        return List.copyOf(legalTargets);
    }

    /** Complete flat target list, including occurrences found illegal during resolution. */
    public List<UUID> getDeclaredTargetIds() {
        return targetIds;
    }

    public void setDeclaredTargetIds(List<UUID> targetIds) {
        this.targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
        this.targetIdsFromAssignments = false;
        this.illegalTargetIndices.clear();
    }

    public void markTargetIllegal(int targetIndex) {
        if (targetIndex >= 0 && targetIndex < targetIds.size()) {
            illegalTargetIndices.add(targetIndex);
        }
    }

    public void replaceTargetIdAt(int targetIndex, UUID targetId) {
        if (targetIndex < 0 || targetIndex >= targetIds.size()) {
            throw new IllegalArgumentException("Invalid target index");
        }
        List<UUID> updated = new ArrayList<>(targetIds);
        updated.set(targetIndex, targetId);
        targetIds = List.copyOf(updated);
        illegalTargetIndices.remove(targetIndex);
    }

    public void replaceTargetCardIdAt(int targetIndex, UUID targetId) {
        if (targetIndex < 0 || targetIndex >= targetCardIds.size()) {
            throw new IllegalArgumentException("Invalid target index");
        }
        List<UUID> updated = new ArrayList<>(targetCardIds);
        updated.set(targetIndex, targetId);
        targetCardIds = List.copyOf(updated);
    }

    /**
     * Whether the target at its original flat-list position is still legal for resolution.
     * Position-sensitive effects use this together with {@link #getDeclaredTargetIds()} so an
     * illegal earlier target does not shift the meaning of later targets.
     */
    public boolean isTargetLegal(int targetIndex) {
        return targetIndex >= 0
                && targetIndex < targetIds.size()
                && !illegalTargetIndices.contains(targetIndex);
    }

    /**
     * Whether an amount-assignment target is still legal for resolution. Assignment maps have unique
     * keys, so their target ids can be mapped back to the canonical flat target position without
     * ambiguity.
     */
    public boolean isAssignmentTargetLegal(UUID targetId) {
        int targetIndex = targetIds.indexOf(targetId);
        if (targetIndex >= 0) {
            return isTargetLegal(targetIndex);
        }
        return targetId != null && targetId.equals(this.targetId);
    }

    /**
     * Card whose {@code target(...)} declarations and effect→group bindings apply to this entry.
     * Aftermath / flashback spells keep the physical parent card on the stack for exile disposition,
     * but targeting and SPELL effects come from {@link Card#graveyardCastHalf()} — without this,
     * a multi-target back half (e.g. Fight) would slice against the front half's groups.
     */
    private Card targetingCard() {
        if (card == null) {
            return null;
        }
        return castWithFlashback ? card.graveyardCastHalf() : card;
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
        if (targetIdsFromAssignments) {
            // The flat list holds assignment keys, not this group's chosen targets; the card's
            // declared group targets the separately stored primary target.
            return targetId != null ? List.of(targetId) : List.of();
        }
        Card targeting = targetingCard();
        List<SpellTarget> groups = targeting == null ? List.of() : targeting.getSpellTargets();
        if (groups.isEmpty()) {
            return group >= 0 && group < targetIds.size() && !illegalTargetIndices.contains(group)
                    ? List.of(targetIds.get(group)) : List.of();
        }
        int firstFlatGroup = 0;
        if (targetId != null && !targetIds.isEmpty()) {
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
            // A target group whose bound effect was gated out of this trigger (its intervening-if
            // was not met, e.g. Noggle Hedge-Mage's independent Islands / Mountains ETBs) chose no
            // targets, so it contributes nothing to the flat list — skip it (consuming 0) so a
            // still-active later group's slice isn't shifted (CR 603.4).
            if (!isTargetGroupActive(g.getIndex())) {
                continue;
            }
            int declared = g.getIndex() < targetGroupSizes.size()
                    ? targetGroupSizes.get(g.getIndex())
                    : g.getMaxTargets();
            int size = Math.min(Math.max(declared, 0), targetIds.size() - consumed);
            if (g.getIndex() == group) {
                List<UUID> legalTargets = new ArrayList<>(size);
                for (int i = consumed; i < consumed + size; i++) {
                    if (!illegalTargetIndices.contains(i)) {
                        legalTargets.add(targetIds.get(i));
                    }
                }
                return List.copyOf(legalTargets);
            }
            consumed += size;
        }
        return List.of();
    }

    /**
     * Whether any effect that will actually resolve on this entry is bound to the given target
     * group. A group with no surviving bound effect (a gated-out intervening-if trigger) consumed
     * no targets from the flat {@link #targetIds} list. Returns {@code true} when the card declares
     * no effect/group mapping, preserving legacy positional slicing for ordinary multi-target
     * spells and abilities (where every declared group is always populated).
     */
    public boolean isTargetGroupActive(int groupIndex) {
        Card targeting = targetingCard();
        // The group-active concept only applies to entries that carry their surviving effects in
        // effectsToResolve (triggered abilities whose intervening-if may have gated some out). Spell
        // entries resolve from card.getEffects(...) and leave effectsToResolve empty — there every
        // declared group is populated, so fall back to legacy positional slicing (all groups active).
        // Flashback/aftermath spells do put SPELL effects into effectsToResolve (from the cast half),
        // so they continue into the binding check below using targetingCard().
        if (targeting == null || effectsToResolve.isEmpty()) {
            return true;
        }
        // A bare positional target group — one no effect is bound to, e.g. Blood Feud's first fight
        // target which the FightTargetsEffect (bound to the second group) reads by index — is never a
        // gated-out trigger group; it always contributes its chosen targets to the flat list.
        if (!targeting.bindsEffectToTargetGroup(groupIndex)) {
            return true;
        }
        for (CardEffect effect : effectsToResolve) {
            if (targeting.getEffectTargetIndex(effect) == groupIndex) {
                return true;
            }
        }
        return false;
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
        Card targeting = targetingCard();
        int group = targeting == null ? -1 : targeting.getEffectTargetIndex(effect);
        if (group < 0) {
            return getTargetIds();
        }
        if (targetIds.isEmpty()) {
            // On an aura the lone targetId is the enchant target (group 0), never a later
            // group's target — an effect bound to a later group simply has no target chosen.
            if (targeting.isAura() && group != 0) {
                return List.of();
            }
            return targetId != null ? List.of(targetId) : List.of();
        }
        return targetsForGroup(group);
    }

    /**
     * The targets chosen for the target group the given effect is bound to, or {@code null} when the
     * effect is not bound to any group.
     *
     * <p>Unlike {@link #targetsForEffect} this never falls back to the flat target list, so a caller
     * can distinguish "bound, but its group's target is gone" (empty list) from "unbound" ({@code null})
     * — the distinction a multi-target card needs when each target drives its own effect.</p>
     */
    public List<UUID> targetsForBoundEffectGroup(CardEffect effect) {
        Card targeting = targetingCard();
        int group = targeting == null ? -1 : targeting.getEffectTargetIndex(effect);
        return group < 0 ? null : targetsForGroup(group);
    }

    private static List<UUID> assignmentTargetIds(Map<UUID, Integer> assignments) {
        return assignments == null || assignments.isEmpty()
                ? List.of()
                : List.copyOf(assignments.keySet());
    }

    public boolean isSingleTarget() {
        return targetId != null && targetIds.isEmpty() && targetCardIds.isEmpty();
    }

    public boolean hasAnyTarget() {
        return targetId != null || !targetIds.isEmpty() || !targetCardIds.isEmpty();
    }
}
