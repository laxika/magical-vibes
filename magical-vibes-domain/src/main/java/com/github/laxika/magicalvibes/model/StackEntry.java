package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class StackEntry {

    @Setter private SubgameResult subgameResult;

    private StackEntryType entryType;
    private StackEntryType bombardmentOriginalEntryType;
    private final Card card;
    private UUID planarAbilityId = UUID.randomUUID();

    /** Independent stack identity for abilities, including abilities with no card source. */
    public UUID getTargetableId() {
        return getCard() == null || sourcePlanarObject != null
                || entryType == StackEntryType.ACTIVATED_ABILITY
                || entryType == StackEntryType.TRIGGERED_ABILITY
                ? planarAbilityId : getCard().getId();
    }

    /** Resolves a legacy source-card reference only when it names one unambiguous stack object. */
    public static UUID resolveTargetableId(List<StackEntry> stack, UUID targetId) {
        if (targetId == null || stack.stream().anyMatch(entry -> entry.getTargetableId().equals(targetId))) {
            return targetId;
        }
        List<StackEntry> matching = stack.stream()
                .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(targetId))
                .toList();
        if (matching.size() > 1) {
            throw new IllegalStateException("Choose a specific ability on the stack");
        }
        return matching.isEmpty() ? targetId : matching.getFirst().getTargetableId();
    }

    /** Copies an ability's context for another trigger or copy that is a distinct stack object. */
    public StackEntry copyForNewStackObject() {
        StackEntry copy = new StackEntry(this);
        copy.planarAbilityId = UUID.randomUUID();
        return copy;
    }

    private Card castCard;
    /**
     * The half of the aftermath split card {@link #aftermathHalfSource} cast as this spell, built on
     * first use by {@link #getCard()}.
     */
    @Getter(AccessLevel.NONE) private Card aftermathHalf;
    @Getter(AccessLevel.NONE) private Card aftermathHalfSource;
    @Getter(AccessLevel.NONE) private boolean aftermathHalfFromGraveyard;
    @Setter private UUID controllerId;
    /** The player who cast this spell when another player has since gained control of it. */
    private UUID casterId;
    /** The player whose upkeep caused this entry's each-upkeep trigger, when applicable. */
    @Setter private UUID activePlayerId;
    @Getter(AccessLevel.NONE) private boolean triggerRecordSettled;
    @Getter(AccessLevel.NONE) private boolean triggerRecordCarried;
    @Setter private String description;
    private List<CardEffect> effectsToResolve;
    /**
     * Each player's card count (cards in hand and nontoken permanents) as this spell began to resolve, its own card
     * counted in its owner's hand when it was cast from there, or {@code null} when none was taken: what "a spell you
     * control causes you to gain card advantage" compares with as the spell finishes resolving. Immutable, so copies
     * share it.
     */
    private Map<UUID, Integer> cardCountsBeforeResolution;
    private List<CardEffect> bombardmentOriginalEffectsToResolve;
    /** Index of the effect currently being dispatched by EffectResolutionService. */
    @Setter private int resolvingEffectIndex = -1;
    /** Spliced steps of a simultaneous {@code SequenceEffect}, each mapped to its sequence's group. */
    @Getter(AccessLevel.NONE)
    private final Map<CardEffect, Integer> simultaneousStepGroups = new IdentityHashMap<>();
    @Getter(AccessLevel.NONE)
    private int nextSimultaneousGroup;
    @Setter private int xValue;
    /**
     * Whether {@link #xValue} was written by the spell's own resolved cast trigger (Naya Soulbeast's
     * revealed cards) rather than chosen as X while casting. A copy of the spell was never cast, so it
     * does not inherit a value derived from that trigger.
     */
    @Setter private boolean xValueSetByCastTrigger;
    /** X chosen for a spell on the stack that a resolving token-copy effect copies (CR 707.2); 0 otherwise. */
    @Setter private int tokenCopyXValue;
    /** Number of modes chosen for the modal spell represented by this entry, when applicable. */
    @Setter private Integer modalModeCount;
    @Setter private int phyrexianManaPaidWithLife;
    /** The ETB mode selected while casting a modal permanent, when it differs from the paid X. */
    @Setter private Integer etbMode;
    @Setter private UUID targetId;
    /** The opponent chosen before that opponent selected the spell's creature target. */
    @Setter private UUID opponentChosenTargetPlayerId;
    /** Controller fixed when choosing a group of targets controlled by the same player. */
    @Setter private UUID requiredTargetControllerId;
    /**
     * Controller of each target permanent when it was chosen, for targets worded "that player controls"
     * ("for each opponent, choose up to one target creature that player controls"). A target whose
     * controller differs on resolution is illegal.
     */
    private final Map<UUID, UUID> targetControllersAtCast = new HashMap<>();
    private boolean targetIdOverriddenForEffectResolution;
    private UUID primaryTargetBeforeEffectResolution;
    private Integer resolvingEffectTargetGroup;
    private final UUID sourcePermanentId;
    private UUID resolutionSourcePermanentId;

    /** Uses a permanent returned during this resolution as the source of subsequent effects. */
    public void useReturnedPermanentAsSource(Permanent permanent, UUID controllerId) {
        resolutionSourcePermanentId = permanent.getId();
        sourcePermanentSnapshot = new Permanent(permanent);
        damageSourceCard = permanent.getCard();
        damageSourceControllerId = controllerId;
    }

    public UUID getSourcePermanentId() {
        return resolutionSourcePermanentId != null ? resolutionSourcePermanentId : sourcePermanentId;
    }
    /** Controller of the source permanent when an activated ability was put on the stack. */
    @Setter private UUID sourcePermanentControllerId;
    @Setter private com.github.laxika.magicalvibes.model.planar.PlanarObject sourcePlanarObject;
    private Map<UUID, Integer> damageAssignments;
    /**
     * Whether this triggered ability announced its targets and division after it was put on the stack, before any
     * player received priority (CR 601.2d, 603.3d), also when it chose no target ("any number of targets" may be none):
     * set by {@link #announceDamageAssignments}, so the division isn't asked again.
     */
    private boolean divisionAnnounced;
    @Getter(AccessLevel.NONE)
    private final Map<UUID, Card> lastKnownPermanentCards = new HashMap<>();
    /** Results of {@code DeterminedOnce} amounts already evaluated during this resolution (CR 608.2h). */
    private final Map<com.github.laxika.magicalvibes.model.amount.DynamicAmount, Integer> amountsDeterminedOnce =
            new HashMap<>();
    /** Effective power just before a permanent leaves during this resolution. */
    private final Map<UUID, Integer> lastKnownPermanentPowers = new HashMap<>();
    /** Effective toughness just before a permanent leaves while an ability that references it is on the stack. */
    private final Map<UUID, Integer> lastKnownPermanentToughnesses = new HashMap<>();
    /**
     * The Auras and Equipment attached to a permanent this entry targets, or names as its non-targeting "that
     * creature", just before it left the battlefield (CR 608.2h): "destroy all Equipment attached to that creature"
     * (Corrosive Ooze) still finds them once the creature is gone. Recorded by the departure whatever the effects are;
     * read through {@link #lastKnownPermanentAttachments}.
     */
    @Getter(AccessLevel.NONE)
    private final Map<UUID, com.github.laxika.magicalvibes.model.rules.DepartedAttachments>
            lastKnownPermanentAttachments = new HashMap<>();
    private final Map<UUID, Map<CounterType, Integer>> lastKnownPermanentCounters = new HashMap<>();
    /** Permanents returned by a previous effect in this resolution, for follow-up effects. */
    private final Set<UUID> returnedPermanentIds = new LinkedHashSet<>();
    /** Owners whose library an earlier effect of this resolution left unshuffled for a later shuffle. */
    private final Set<UUID> deferredLibraryShuffleOwnerIds = new LinkedHashSet<>();
    /** Effective colors of declared targets just before they left the battlefield. */
    private final Map<UUID, Set<CardColor>> lastKnownTargetColors = new HashMap<>();
    /** Controllers remembered before earlier effects in this resolution remove their permanents. */
    private final Map<UUID, UUID> removedPermanentControllers = new HashMap<>();
    private final Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
    /** Counters a permanent spell is instructed to enter with. */
    private final Map<CounterType, Integer> enteringCounters = new EnumMap<>(CounterType.class);
    /** Card id of the spell whose stack object is the source of this ability. */
    @Setter private UUID sourceStackCardId;
    /** Colored mana spent to activate this ability, snapshotted so later activations cannot overwrite it. */
    @Setter private Map<ManaColor, Integer> activationManaSpent = Map.of();
    /** Amount of Treasure-produced mana spent to activate this ability, snapshotted at activation. */
    @Setter private int activationTreasureManaSpent;
    /** Whether mana produced by a Treasure was spent to activate this ability. */
    @Setter private boolean activationUsedTreasureMana;
    /**
     * Mana spent to cast this spell, retained until a permanent spell enters the battlefield. The cast trigger collection
     * records it again as the spell becomes cast (CR 601.2i), so a cast trigger's "the amount of mana spent to cast that
     * spell" reads it as it resolves, from the entry's snapshot of the spell ({@link #manaSpentToCastThisSpell}).
     */
    @Setter private int manaSpentToCast;
    /**
     * Mana produced by creatures that was spent to cast this spell, recorded beside {@link #manaSpentToCast} as the spell
     * becomes cast ("if at least three mana from creatures was spent to cast it", Inga and Esika).
     */
    @Setter private int creatureManaSpentToCast;
    /**
     * The colors of mana spent to cast this spell (colorless is no color, CR 105.1), recorded as it becomes cast, so a
     * cast trigger's "for each color of mana spent to cast that spell" reads them as it resolves: from the spell on the
     * stack, else from the entry's snapshot of it once it has left (CR 608.2h; Magmablood Archaic).
     */
    private Set<ManaColor> colorsSpentToCast = Set.of();
    /**
     * The mana of each color spent to cast this spell, recorded as it becomes cast; the permanent it resolves into keeps
     * it ({@code Permanent.manaSpentByColorToCast}) for its own enters abilities' {@code ColorManaPairsSpentToCast}.
     */
    private Map<ManaColor, Integer> manaSpentByColorToCast = Map.of();
    /** Additional entry counters granted by costs paid for the spell, also retained by copies. */
    @Setter private int additionalEnterCounters;
    @Setter private Zone targetZone;
    @Setter private List<UUID> targetCardIds;
    /** Target counts per independently optional graveyard target group, in group order. */
    @Setter private List<Integer> targetCardGroupSizes = List.of();
    /** Cards chosen from the battlefield by a mixed battlefield-and-graveyard target choice. */
    private final Set<UUID> battlefieldChosenTargetCardIds = new HashSet<>();
    private Map<CardEffect, List<UUID>> targetCardIdsByEffect = Map.of();
    @Setter private TargetFilter targetFilter;
    /** Per-position filters declared by a multi-target activated ability. */
    @Setter private List<TargetFilter> targetFilters = List.of();
    /** Cross-target restriction declared by a multi-target activated ability. */
    @Setter private MultiTargetConstraint multiTargetConstraint;
    @Setter private boolean copy;
    /**
     * CR 613.7d timestamp this spell received when it was put on the stack (0 for abilities and
     * spells not yet stamped). It orders the continuous effects of the spell's abilities that
     * function on the stack ({@code WrappedStackStaticEffect}).
     */
    @Setter private long stackTimestamp;
    /** Whether this copy was created and then cast (CR 707.12), so it resolves as a cast spell. */
    @Setter private boolean castCopy;
    @Setter private boolean nonTargeting;
    /**
     * Whether this triggered ability is a reflexive trigger ("When you do, ...", CR 603.12) created
     * while another ability resolved. It is not a Saga chapter ability even when a Saga is its source.
     */
    @Setter private boolean reflexiveTrigger;
    /** Whether an effect already placed the physical spell card in its final zone. */
    @Setter private boolean spellDispositionHandled;
    @Setter private boolean returnToHandAfterResolving;
    /** When set, the resolved spell card is put into its owner's library at this 0-based position from the
     *  top instead of going to the graveyard (Approach of the Second Sun's "seventh from the top" = 6). */
    @Setter private Integer putIntoLibraryPositionAfterResolving;
    @Setter private boolean spellMovedDuringResolution;
    @Setter private boolean castWithFlashback;

    /** Flashforward replaces every destination when this spell leaves the stack. */
    @Setter private boolean castWithFlashforward;
    /** Whether this spell was cast using an escape permission. */
    @Setter private boolean castWithEscape;
    /** This creature spell targets a creature to merge with as it resolves. */
    @Setter private boolean castWithMutate;
    /** Cards exiled from the graveyard to pay an escape cost, linked when the permanent enters. */
    @Setter private List<UUID> escapeExiledCardIds = List.of();
    /** Whether Feather's replacement effect should exile this spell and return it at the next end step. */
    @Setter private boolean exileAndReturnToHandAtNextEndStep;
    /** Source of the delayed return ability: the Feather trigger that created it (CR 603.7e). */
    @Setter private Card exileAndReturnSourceCard;
    /**
     * Whether a replacement effect applies to this spell: "if that spell would be put into a graveyard,
     * exile it instead" (The Dawning Archaic, Chancellor of the Spires). Unlike
     * {@link #castWithFlashback} this only replaces the graveyard disposition, so return-to-hand /
     * into-library dispositions still win.
     */
    @Setter private boolean exileInsteadOfGraveyard;
    /** Whether a permanent cast through a graveyard permission is exiled if it would leave. */
    @Setter private boolean exilePermanentIfLeavesBattlefield;
    /** Source permanent to record when an ExileSpellEffect tracks the spell in exile. */
    @Setter private UUID exileWithSourcePermanentId;
    /**
     * Further source permanents whose replacement effect would also exile this spell (a second Rod of
     * Absorption); the spell's controller picks which one applies as it resolves (CR 616.1).
     */
    private final List<UUID> competingExileSourcePermanentIds = new ArrayList<>();
    /** Whether this spell goes to the bottom of its owner's library instead of a graveyard. */
    @Setter private boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard;
    /** Whether this spell was cast via Disturb (CR 702.146) — enters transformed; exile on leave-to-GY. */
    @Setter private boolean castWithDisturb;
    @Setter private boolean castWithOmen;
    /** Whether this spell was cast via Adventure; after resolving, its physical card is exiled with permission to cast the front face. */
    @Setter private boolean castWithAdventure;
    /** Whether this permanent spell was cast for its Warp cost. */
    @Setter private boolean castWithWarp;
    /**
     * Whether this spell was cast transformed without paying its mana cost after a Siege battle
     * was defeated. Enters as the back face (like Disturb) but uses normal spell disposition on fizzle.
     */
    @Setter private boolean castTransformed;
    /** Whether a creature spell resolves as a face-down 2/2 from a morph cast. */
    @Setter private boolean castFaceDown;
    /** Whether the face-down casting alternative was disguise and supplies ward. */
    @Setter private boolean castWithDisguise;
    /** Whether this face-down creature turns face up when it would deal or receive damage or become tapped. */
    @Setter private boolean faceDownTurnsFaceUpOnDamageOrTap;
    /** Whether a permanent resolved from this spell enters the battlefield tapped. */
    @Setter private boolean entersTapped;
    @Setter private Zone sourceZone;
    /** Whether this is the activated ability created by cycling a card from hand. */
    @Setter private boolean cyclingAbility;
    /** Whether this is a planeswalker loyalty ability, as opposed to another ability the planeswalker has. */
    @Setter private boolean loyaltyAbility;
    /** Cards exiled from the graveyard to pay this spell's delve cost. */
    @Setter private List<UUID> delvedCardIds = List.of();
    /** The cards exiled to pay this spell's delve cost, kept even after they leave exile (Soulflayer). */
    @Setter private List<Card> delvedCards = List.of();
    /**
     * Overrides the card's disposition owner when this spell is controlled by someone other than its
     * owner (e.g. cast from an opponent's hand via Sen Triplets). Null for the overwhelming majority of
     * casts, where owner == controller; {@link #getOwnerId()} then falls back to {@link #controllerId}.
     * Read by the graveyard/hand/library disposition paths and the permanent-spell entry ownership stamp.
     */
    @Setter private UUID ownerIdOverride;
    @Setter private boolean kicked;
    /** Whether this spell's optional Gift was promised while casting it. */
    @Setter private boolean giftPromised;

    /** Whether this spell paid a kicker or multikicker cost. */
    public boolean wasKicked() {
        if (kicked && card.getStaticEffects().stream()
                .filter(com.github.laxika.magicalvibes.model.effect.KickerEffect.class::isInstance)
                .map(com.github.laxika.magicalvibes.model.effect.KickerEffect.class::cast)
                .findFirst().map(com.github.laxika.magicalvibes.model.effect.KickerEffect::countsAsKicker)
                .orElse(true)) {
            return true;
        }
        if (repeatedAdditionalCosts.isEmpty()) {
            return false;
        }
        return card.getCastingCosts().stream()
                .filter(RepeatableAdditionalManaCost.class::isInstance)
                .map(RepeatableAdditionalManaCost.class::cast)
                .anyMatch(cost -> cost.multikickerPaymentCount(repeatedAdditionalCosts) > 0);
    }
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
    /** Whether this spell's optional collect-evidence additional cost was paid. */
    @Setter private boolean collectEvidenceCostPaid;
    /** Whether this spell's optional behold additional cost was paid. */
    @Setter private boolean beholdCostPaid;
    /** The last-known power of the permanent or card used to pay this spell's behold cost. */
    @Setter private int beholdPower;
    /** The triggering permanent's keywords when it left the battlefield, or null while it is still there. */
    @Setter private Set<Keyword> triggeringPermanentKeywordsAtLeave;
    /** The permanent used to pay this spell's behold cost, when the choice was from the battlefield. */
    @Setter private UUID beholdPermanentId;
    /** Whether this spell's optional reveal-a-card-from-hand additional cost was paid. */
    @Setter private boolean revealCardFromHandCostPaid;
    /** Whether this spell's optional casualty additional cost was paid. */
    @Setter private boolean casualtyCostPaid;
    /** Whether this spell's optional waterbend additional cost was paid. */
    @Setter private boolean waterbendCostPaid;
    /** Whether this spell's optional teamwork additional cost was paid. */
    @Setter private boolean teamworkCostPaid;
    /**
     * The individual mana payments the caster chose for this spell's
     * {@link com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost}, one entry
     * per repetition ("you may pay {1}{R} and/or {1}{G} any number of times"). Snapshotted at cast
     * time and read back at resolution by
     * {@link com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount}.
     */
    @Setter private List<String> repeatedAdditionalCosts = List.of();
    /**
     * Whether this spell used its own conditional instant-speed permission. Read by the Mirage flash clause
     * ({@code FlashCastWithCleanupSacrificeEffect}), which flags the entering permanent for
     * sacrifice at the next cleanup step when it is set.
     */
    @Setter private boolean castWhenSorceryCouldNotBeCast;
    /** Whether this spell was cast during its controller's precombat or postcombat main phase. */
    @Setter private boolean castDuringMainPhase;
    /** Whether this spell was cast for its evoke (alternate) cost — carried to the entering permanent. */
    @Setter private boolean evoked;
    private Card bestowOriginalCard;
    private Card physicalCard;
    /** Whether this spell was cast for its prowl cost — carried to the entering permanent so its
     *  "if its prowl cost was paid" ETB trigger can gate on it (CR 702.75). */
    @Setter private boolean prowl;
    /** Whether this spell was cast for its spectacle cost. */
    @Setter private boolean spectacle;
    /** Whether this creature spell was cast for its Sneak alternate cost. */
    @Setter private boolean sneak;
    @Setter private boolean castForForetell;
    /** The foreteller's turn count when this spell was exiled by foretell. */
    @Setter private int foretellControllerTurnsAtExile = -1;
    /** Every player's turns-begun count when this spell was exiled by foretell; empty when unknown. */
    @Setter private Map<UUID, Integer> foretellTurnsTakenAtExile = Map.of();
    @Setter private boolean alternateCost;
    /** Mana value of the creature returned to pay this spell's web-slinging cost, when applicable. */
    @Setter private Integer webSlingingReturnedCreatureManaValue;
    /** Whether this spell was cast for its madness cost. */
    @Setter private boolean madness;
    /** Whether this spell was cast for its overload cost (CR 702.96a): every "target" in its text
     *  reads "each", and per CR 702.96b the spell has no targets at all. */
    @Setter private boolean overloaded;
    /** Whether the spell's controller controlled a Mount when the spell was finished being cast. */
    @Setter private boolean controlledMountAsCast;
    /** Whether the spell's controller controlled a Dragon when the spell was finished being cast. */
    @Setter private boolean controlledDragonAsCast;
    /** Whether the spell's controller controlled a Faerie when the spell was finished being cast. */
    @Setter private boolean controlledFaerieAsCast;
    /** Whether the spell's controller controlled a modified creature when the spell was finished being cast. */
    @Setter private boolean controlledModifiedCreatureAsCast;
    /** Card exiled as an additional behold cost, pending the permanent spell entering. */
    /** The hand card revealed to pay a power-tracking additional cost. */
    @Setter private UUID lastManaPaymentPlayerId;
    @Setter private List<Card> cardsMilledAsActivationCost = List.of();
    @Setter private Card revealedPowerCostCard;
    /** The revealed card's last power while it remained in its owner's hand. */
    @Setter private int revealedPowerCostCardLastKnownPower;
    @Setter private Card beheldCard;
    @Setter private UUID beheldCardOwnerId;
    @Setter private CardSubtype beholdChosenSubtype;
    @Setter private CardSubtype chosenCreatureType;
    private final Map<UUID, CardSubtype> chosenCreatureTypes = new HashMap<>();
    @Setter private Card damageSourceCard;
    /** Controller of a distinct damage source when it differs from the ability's controller. */
    @Setter private UUID damageSourceControllerId;
    /** Carries Demonfire's replacement through redirected damage during this resolution. */
    @Setter private boolean exilesCreaturesDamaged;
    /** Whether a continuation entry still deals damage as part of resolving its source spell. */
    @Setter private boolean spellDamageContinuation;
    @Setter private int stateTriggerEffectIndex = -1;
    @Setter private UUID attackedTargetId;
    /** Exile incarnation watched by a delayed return; entering exile again advances this version. */
    @Setter private long triggeringCardExileEntryVersion = -1L;
    /** Creatures whose declared attack caused this ability, retained for last-known information. */
    private List<Permanent> attackingPermanentSnapshots = List.of();
    /** Defending player captured while the attacked object still exists. */
    @Setter private UUID defendingPlayerId;
    /** Whether this spell or ability has already been counted for a pile grouping or guess this turn. */
    @Setter private boolean causedPileGroupingOrGuessThisTurn;
    /**
     * The integer payload of the event (or prior resolution step) behind this entry — life gained,
     * damage dealt, excess damage, etc. Snapshotted by the trigger collector that enqueues the entry
     * (parallel to {@link #xValue}, but for trigger-event data rather than cast-time data), or set by
     * an earlier effect on the same entry (e.g. excess damage from a damage effect). Read back by the
     * {@code EventValue} dynamic amount at resolution.
    */
    @Setter private int eventValue;
    /** Number of cards looked at by the scry event that caused this triggered ability. */
    @Setter private int cardsLookedAtWhileScrying;
    @Setter private Integer combatOpponentPowerAtTrigger;
    @Setter private Integer combatOpponentToughnessAtTrigger;
    @Setter private boolean gainLifeEqualToGreatestPowerOfCardsPutIntoGraveyard;
    /** Records the cards actually moved to the graveyard by the current library choice. */
    @Setter private boolean recordLibraryGraveyardCount;
    /**
     * The limit key ({@code TriggeredAbility.limitKey()}) of the "do this only once each turn" ability this entry is
     * of (CR 603.2h), or {@code null}: taking the ability's action as the entry resolves marks that ability of the
     * source as used this turn, and no other ability of the source.
     */
    @Setter private String onceEachTurnActionKey;
    /**
     * The types of mana the tap for mana that triggered this ability produced ("the mana that land produced", "one
     * mana of any type that permanent produced"), set by the tap collector, or by the any-color choice that settles it
     * for an ability parked while it was pending.
     */
    private Set<ManaColor> producedManaColors = Set.of();
    /**
     * Whether this mana-tap ability was parked in {@code GameData.pendingManaAbilityTriggers} until the color of the mana
     * its tap produced is chosen, because its effects read {@link #getProducedManaColors()}.
     */
    @Setter private boolean awaitingProducedManaColor;
    /**
     * The players who said yes to the "each player may" choice of this resolution ("each opponent who discarded their
     * hand this way"), set by that choice before its follow-up resolves.
     */
    private List<UUID> acceptedPlayerIds = List.of();
    /**
     * The permanent that granted the triggered ability this entry is ({@link TriggeredAbility#grantingPermanentId()}):
     * "sacrifice this Equipment", "transform Optimus Prime", "other than Animal Friend" in an ability an Aura,
     * Equipment or one-shot grant gave another permanent. Set by the collector that puts the ability on the stack;
     * {@code null} for an ability that wasn't granted. An effect that names the granting permanent fails loudly when it
     * resolves without one, so a collector that drops it is found rather than resolving against the wrong object.
     */
    @Setter private UUID grantingPermanentId;
    @Setter private Integer dyingPermanentManaValue;
    /**
     * The mana value limit of the graveyard cards this triggered ability targets, fixed from the trigger event as its
     * targets were chosen while it was put on the stack (CR 603.3d): a death trigger's
     * {@code ReturnCardFromGraveyardEffect.dynamicMaxManaValue()} evaluated against the object that died as it last
     * existed (CR 603.10a), so "with lesser mana value" and Soulshift X keep the value the event gave them. {@code null}
     * when the limit, if any, is evaluated as the ability resolves.
     */
    @Setter private Integer graveyardTargetMaxManaValue;
    /**
     * The card the source's linked exiling ability exiled (CR 607.2a), as it was when this ability triggered: Hoarding
     * Dragon's "the exiled card". Set by the collector that puts the ability on the stack, so the source returning and
     * exiling another card can't redirect it. {@code null} when the source had exiled none.
     */
    @Setter private UUID linkedExiledCardId;
    /**
     * The triggering permanent's characteristics as it last existed on the battlefield (its types, subtypes and colors
     * as the layers left them, CR 608.2h), recorded as it leaves before this ability resolves: "if that land is a
     * Forest". {@code null} while it is on the battlefield, or when this entry has no triggering permanent.
     */
    @Setter private Card triggeringPermanentLastKnownCard;
    /** Permanent ids that received counters during the current effect resolution. */
    private final List<UUID> counteredPermanentIdsThisResolution = new ArrayList<>();
    /** Controller of the spell targeted by a counter effect, retained after that spell leaves the stack. */
    @Setter private UUID counteredSpellControllerId;
    /**
     * The per-permanent player payload behind this entry — one entry per permanent involved in the
     * event, holding that permanent's controller. Stamped by
     * {@code DestroyAllPermanentsEffectHandler} onto a rider entry with the controller of every
     * permanent actually destroyed, so "for each permanent destroyed this way, … that permanent's
     * controller …" riders can act once per permanent rather than once per player (Stench of Evil).
     * Duplicates are meaningful: a player who lost three lands appears three times.
     */
    @Setter private List<UUID> eventPlayerIds = List.of();
    /** Controllers of the nontoken permanents actually destroyed by the event, positionally aligned
     * with the nontoken subset of {@link #eventPlayerIds}. */
    @Setter private List<UUID> eventNontokenPlayerIds = List.of();
    /** Controllers of every permanent a destroy instruction was applied to, whether or not it was
     * actually destroyed ("for each of those permanents"), one entry per permanent. */
    @Setter private List<UUID> eventTargetedPlayerIds = List.of();
    /** Controllers of the permanents destroyed by the event wherever they ended up (a destroyed
     * permanent exiled instead of dying still counts as destroyed), one entry per permanent. */
    @Setter private List<UUID> eventDestroyedPlayerIds = List.of();
    /** Card ids of the permanents actually destroyed by the event that produced this entry. */
    @Setter private List<UUID> eventCardIds = List.of();
    private final Map<UUID, Long> eventCardGraveyardEntryVersions = new HashMap<>();
    /** Cards exiled during this entry's resolution, excluding earlier uses of the same source. */
    private List<UUID> resolutionExiledCardIds = List.of();

    public void setProducedManaColors(Set<ManaColor> colors) {
        producedManaColors = Set.copyOf(colors);
    }

    public void setAcceptedPlayerIds(List<UUID> playerIds) {
        acceptedPlayerIds = List.copyOf(playerIds);
    }

    /**
     * Records {@code dealer}, controlled by {@code controllerId}, as the permanent that dealt the damage this trigger
     * event was about ({@link #getEventDamageSource()}); the entry keeps a copy of its own.
     */
    public void setEventDamageSource(Permanent dealer, UUID controllerId) {
        this.eventDamageSource = dealer == null ? null : new Permanent(dealer);
        this.eventDamageSourceControllerId = dealer == null ? null : controllerId;
    }

    /**
     * The permanent that dealt the damage this trigger event was about ({@link #getEventDamageSource()}), for an
     * effect whose damage it deals.
     *
     * @throws IllegalStateException when the ability reached the stack without one
     */
    public Permanent requireEventDamageSource() {
        if (eventDamageSource == null) {
            throw new IllegalStateException(description + " has the creature that dealt the damage deal damage, but "
                    + "reached the stack without it: its collector must record the event's damage source "
                    + "(setEventDamageSource)");
        }
        return eventDamageSource;
    }

    /**
     * The permanent that granted this triggered ability ({@link #getGrantingPermanentId()}), for an effect that names it.
     *
     * @throws IllegalStateException when the ability reached the stack without its granting permanent
     */
    public UUID requireGrantingPermanentId() {
        if (grantingPermanentId == null) {
            throw new IllegalStateException(description + " names the permanent that granted it, but reached the "
                    + "stack without one: its collector must copy TriggeredAbility.grantingPermanentId() to the entry");
        }
        return grantingPermanentId;
    }

    public void setResolutionExiledCardIds(List<UUID> cardIds) {
        resolutionExiledCardIds = List.copyOf(cardIds);
    }
    /** The resolution scratchpad: results an earlier step left for later steps to read. */
    private ResolutionResults resolutionResults = ResolutionResults.EMPTY;

    /**
     * Records the departures of the step being resolved, replacing those of earlier steps. A removal
     * step calls this even when nothing left (an empty list), so "if you do" reads this step's result.
     */
    public void recordDepartures(List<DepartureResult> departures) {
        resolutionResults = resolutionResults.withDepartures(departures);
    }

    /**
     * Records the cards the step being resolved actually drew, replacing those of earlier steps. A draw
     * step calls this even when nothing was drawn (an empty list), so "if you do" reads this step's result.
     */
    public void recordDrawnCards(List<UUID> drawnCardIds) {
        resolutionResults = resolutionResults.withDrawnCardIds(drawnCardIds);
    }

    /**
     * Records whether the counter step being resolved actually countered its target spell, replacing that
     * of earlier steps. A counter step calls this even when nothing was countered (false), so "if that
     * spell is countered this way" reads this step's result.
     */
    public void recordCounteredSpell(boolean countered) {
        resolutionResults = resolutionResults.withCounteredSpell(countered);
    }

    /**
     * Records whether the copy step being resolved copied its spell, replacing that of earlier steps. A copy step
     * calls this even when nothing was copied (false), so "if you don't copy a spell this way" reads this step's
     * result.
     */
    public void recordCopiedSpell(boolean copied) {
        resolutionResults = resolutionResults.withCopiedSpell(copied);
    }

    /** Records what a player picked in the step being resolved, for the next step to act on. */
    public void recordPicks(List<UUID> pickedIds) {
        resolutionResults = resolutionResults.withPickedIds(pickedIds);
    }
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
     * The Auras and Equipment attached to the source as it last existed on the battlefield (CR 608.2h), by permanent id:
     * recorded with {@link #sourcePermanentSnapshot} as the source leaves, whatever the effects are, for "for each Aura
     * and Equipment attached to this creature" read once it has left. Empty while the source is on the battlefield.
     * Set through {@link #setSourceAttachmentIds}; immutable, so copies share it.
     */
    private List<UUID> sourceAttachmentIds = List.of();
    /** Equipment permanent ids sacrificed with the source as part of its activation cost. */
    @Setter private List<UUID> sacrificedAttachedEquipmentIds = List.of();
    /** Last-known snapshot of the permanent attached to the source Aura when its trigger fired. */
    @Setter private Permanent attachedPermanentSnapshot;
    /**
     * The permanent chosen while activating an ability or resolving a library selection. Read back
     * at resolution by the {@code ChosenPermanentPower} dynamic amount so an effect can scale to
     * that permanent's power as the ability resolves.
     */
    @Setter private UUID chosenPermanentId;
    /** Last-known effective power of a permanent chosen by this stack object. */
    @Setter private Integer chosenPermanentPowerAtLastKnown;
    /** Last-known effective toughness of a permanent chosen for stationing. */
    @Setter private Integer chosenPermanentToughnessAtLastKnown;
    /** Graveyard card whose power and toughness were captured just before a graveyard return moved it. */
    @Setter private UUID returnedGraveyardCardId;
    /** Power of {@link #returnedGraveyardCardId} as it last existed in the graveyard. */
    @Setter private Integer returnedGraveyardCardPowerAtLastKnown;
    /** Toughness of {@link #returnedGraveyardCardId} as it last existed in the graveyard. */
    @Setter private Integer returnedGraveyardCardToughnessAtLastKnown;
    /** Card chosen from exile for a creature-or-warped-card cost or a library-search follow-up. */
    @Setter private Card chosenObjectCard;
    /** Permanents placed onto the battlefield by the preceding library search. */
    @Setter private List<UUID> searchedPermanentIds = List.of();
    /**
     * Last-known card id of the event that produced this triggered ability, when an effect needs to
     * act on "that card" rather than a chosen target — e.g. the creature that died for Seraph's
     * {@code Triggers.creatureDies(DAMAGED_BY_THIS)} return. Not a target: it is never validated or fizzled.
    */
    @Setter private UUID triggeringCardId;
    @Setter private boolean triggerOrderChosen;
    /** Last-known card characteristics of the card returned from a graveyard to hand for a triggered ability. */
    @Setter private Card triggeringCardSnapshot;
    /** The targets the triggering spell had when this ability triggered, kept in case the spell leaves the stack first. */
    @Setter private List<UUID> triggeringSpellTargetIds = List.of();
    /**
     * The spell a spell-cast trigger was about ("that spell", "its controller", "its colors", "its mana value"), or the
     * activated ability an activation trigger was about ({@link #triggeringActivatedAbility}), or the scheme a "whenever
     * you set a scheme in motion" trigger was about (its set-in-motion stack object), or the triggered ability an
     * "an attacking creature causes a triggered ability of it to trigger" trigger was about (Firebender Ascension), as it
     * was on the stack
     * when the ability triggered: the last-known information the effects use once that spell has left
     * the stack (CR 608.2h), while it is still there they read it live. {@code null} when the event was about no spell.
     * Set through {@link #setTriggeringSpell}, which keeps a copy of its own.
     */
    private StackEntry triggeringSpellSnapshot;
    /**
     * The triggering spell's place among its caster's spells cast this turn ({@code GameData.getSpellsCastThisTurn}), or
     * {@code -1} when none was recorded: the spells before it there are the ones its caster cast "before it this turn"
     * (Thousand-Year Storm). Set by the cast collector as the ability triggers, so a spell cast in response, or the same
     * card cast again, doesn't move it.
     */
    @Setter private int triggeringSpellCastIndex = -1;
    /**
     * The triggering spell's place in the turn's cast order across every player ({@code GameData
     * .getSpellCastOrderThisTurn}), or {@code -1} when none was recorded: the spells before it there are every spell
     * "cast before it this turn" (storm, CR 702.40a). Set as the ability triggers, as {@link #triggeringSpellCastIndex}
     * is, so a spell cast in response, or the same card cast again, doesn't move it.
     */
    @Setter private int triggeringSpellTurnCastIndex = -1;
    /**
     * The activated ability an activation trigger was about, when the event was an activation: with
     * {@link #triggeringSpellSnapshot} holding that ability's stack entry as it was when this ability triggered, "copy
     * that spell or ability" copies the ability (Bill Potts). {@code null} otherwise. Immutable, so copies share it.
     */
    @Setter private ActivatedAbility triggeringActivatedAbility;
    /**
     * The graveyard incarnation {@link #triggeringCardId} had as the ability triggered, or {@code -1} when none was
     * recorded: a card that left the graveyard and came back is a new object (CR 400.7). The stay of the card the event
     * was about only (the discarded, milled or dying card); the ability's own source card's stay is
     * {@link #sourceGraveyardEntryVersion}.
     */
    @Setter private long triggeringCardGraveyardEntryVersion = -1;
    /**
     * The graveyard incarnation the ability's source card had as the ability triggered from a graveyard (or as an
     * activated ability's cost put it there), or {@code -1} when none was recorded: "if this card is in your graveyard"
     * is false, and "return / exile this card" does nothing, for a card that left the graveyard and came back, a new
     * object (CR 400.7). Read through {@link #sourceCardGraveyardEntryVersion()}.
     */
    @Setter private long sourceGraveyardEntryVersion = -1;
    /**
     * The permanent the trigger event was about, as it last existed on the battlefield (CR 603.10a, CR 608.2h): the
     * creature that died, the permanent that left or was sacrificed. Effects read "its counters", "its name", "its
     * mana value" and "that creature" from it as they resolve; {@code null} when the event was about no departed
     * permanent. Set through {@link #setTriggeringPermanent}, which keeps a copy of its own.
     */
    private Permanent triggeringPermanentSnapshot;
    /** The Auras and Equipment attached to {@link #triggeringPermanentSnapshot} just before it left (CR 603.10a). */
    private com.github.laxika.magicalvibes.model.rules.DepartedAttachments triggeringAttachments =
            com.github.laxika.magicalvibes.model.rules.DepartedAttachments.NONE;
    /**
     * The permanent that dealt the damage a damage trigger event was about, as it dealt the damage ("that Archer" of
     * Greatbow Doyen, "it" of "whenever a creature you control deals combat damage to a player, it deals that much
     * damage to …"): set by the collector that puts such an ability on the stack through
     * {@link #setEventDamageSource}, and read by an effect whose damage that permanent deals
     * ({@code DamageSource.EVENT_DAMAGE_SOURCE}, CR 608.2h). {@code null} when the event was about no single
     * damage-dealing permanent.
     */
    private Permanent eventDamageSource;
    /** The controller of {@link #eventDamageSource} as it dealt the damage. */
    private UUID eventDamageSourceControllerId;
    /** {@link #eventDamageSource}'s keywords when it left the battlefield, or null while it is still there. */
    @Setter private Set<Keyword> eventDamageSourceKeywordsAtLeave;
    /** Mixed-zone card targets remember their original permanent identity or graveyard incarnation. */
    private final Map<UUID, UUID> mixedZoneTargetPermanentIds = new HashMap<>();
    private final Map<UUID, Long> mixedZoneTargetGraveyardVersions = new HashMap<>();
    /** Graveyard incarnation of each targeted card when it was chosen, keyed by card id. */
    private final Map<UUID, Long> targetCardGraveyardVersions = new HashMap<>();
    /** Graveyard entry chosen as this spell's primary target when it was cast. */
    @Setter private long targetGraveyardEntryVersion = -1;
    /**
     * The cards a "one or more cards" trigger event was about ("those cards", "the discarded cards"), as they were in
     * the event: the cards discarded (by one discard event or by the discard a resolving ability made), put into a
     * graveyard from a library, exiled together, sought or that left a graveyard, or the land a land-play event was
     * about.
     * Empty when the event was about no cards.
     * Set through {@link #setTriggeringCards}; immutable, so copies share it.
     */
    private List<Card> triggeringCards = List.of();
    /** Card id of the permanent sacrificed as an additional cost to cast this spell, when one was paid. */
    @Setter private UUID sacrificedCardId;
    /** Card ids of permanents sacrificed as an activated-ability cost, retained for later effects. */
    @Setter private List<UUID> sacrificedCardIds = List.of();
    /** Graveyard incarnation of each sacrificed card when it was sacrificed as a cost, keyed by card id. */
    @Setter private Map<UUID, Long> sacrificedCardGraveyardVersions = Map.of();
    /** Last-known card of the permanent sacrificed as an additional cost to cast this spell. */
    @Setter private Card sacrificedCardSnapshot;
    /** Last-known card discarded as an activation cost, when an effect refers to that card. */
    @Setter private Card discardedCardSnapshot;
    @Setter private Permanent sacrificedPermanentSnapshot;
    /** Effective power of the permanent sacrificed as an additional cost, when snapshotted. */
    @Setter private int sacrificedPower;
    /** Effective color count of the permanent sacrificed during resolution, when snapshotted. */
    @Setter private int sacrificedColorCount;
    /** Effective toughness of the permanent sacrificed as an additional cost, when snapshotted. */
    @Setter private int sacrificedToughness;
    /** Permanents tapped to pay this spell's convoke cost, captured for effects that refer to them. */
    private List<UUID> convokeCreatureIds = List.of();
    /** Remaining convoke creatures for a resolving effect that makes them connive one at a time. */
    private List<UUID> convokeConniveCreatureIdsToProcess;
    /** Remaining target creatures for a resolving effect that makes them connive one at a time. */
    private List<UUID> targetConniveCreatureIdsToProcess;
    /** Permanents chosen to pay a cost and retained for a later effect in the same ability. */
    private List<UUID> chosenCostPermanentIds = List.of();
    /** Last-known snapshots of permanents chosen to pay a tracked cost. */
    private List<Permanent> chosenCostPermanentSnapshots = List.of();
    /** Last-known card characteristics of the permanent sacrificed as an additional cast cost. */
    private Card sacrificedCard;
    /** Card id of the creature exiled as an additional cost to cast this spell, when one was paid. */
    @Setter private UUID exiledCostCardId;
    /** Last-known card of the creature exiled as an additional cost to cast this spell. */
    @Setter private Card exiledCostCardSnapshot;
    /** Cards exiled to pay the source permanent's graveyard threshold cost for this activation. */
    @Setter private List<UUID> activatedAbilityExiledCardIds = List.of();
    /**
     * Id of the permanent whose event produced this triggered ability, when an effect needs to act on
     * "it" rather than a chosen target — e.g. the permanent that became tapped for Freyalise's Winds'
     * {@code Triggers.permanentYouControlBecomesTapped()} / {@code Triggers.permanentOpponentControlsBecomesTapped()}
     * counter.
     * Not a target: it is never validated or fizzled.
     */
    @Setter private UUID triggeringPermanentId;
    /**
     * Every permanent that entered together for a batched "one or more creatures enter" trigger, so
     * a resolution-time choice can be limited to the group that caused the trigger. Not a target.
     */
    @Setter private List<UUID> triggeringPermanentIds = List.of();
    /**
     * The player a trigger event was about, for the effects that read "that player" from it: the player who became the
     * monarch. Not a target. Most other collectors still give "that player" as the non-targeting {@link #targetId}.
     */
    @Setter private UUID triggeringPlayerId;
    /**
     * The kind of counters a counter event was about ("the same number and kind of counters"), with
     * {@link #eventValue} as their number; {@code null} for an event about no one kind. Not a target.
     */
    @Setter private CounterType eventCounterType;
    /**
     * Whether this ability of an Aura triggered as the permanent it was attached to left, before the Aura itself is put
     * into its owner's graveyard as a state-based action (CR 704.5m): the Aura's next graveyard stay is the one "return
     * this card" means, and {@code GameData.markGraveyardEntry} records it as {@link #sourceGraveyardEntryVersion}.
     */
    @Setter private boolean awaitingSourceGraveyardEntry;
    /**
     * The vote a "whenever players finish voting" ability triggered on, as the players finished it (Erestor of the
     * Council, Model of Unity). Immutable. Not a target.
     */
    @Setter private VotingResult triggeringVotingResult;
    /** Controller of the triggering permanent when its non-targeting reference was captured. */
    @Setter private UUID triggeringPermanentControllerId;
    /** Owner of the triggering permanent when its non-targeting reference was captured. */
    @Setter private UUID triggeringPermanentOwnerId;
    /** Power and toughness captured for a permanent when its trigger was created. */
    @Setter private Integer triggeringPermanentPowerAtTrigger;
    /** The source permanent's power, static bonuses included, when the ability triggered (last known information). */
    @Setter private Integer sourcePowerAtTrigger;
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
     * Whether target group 0 was stored in {@link #targetId} when this entry was created while the
     * remaining groups were stored in {@link #targetIds}. Resolution temporarily updates
     * {@code targetId} to the target of the current effect, so the target-list layout cannot be
     * inferred from its current value.
     */
    @Setter private boolean primaryTargetStoredSeparately;
    /**
     * How many targets each declared target group actually contributed to the flat {@link #targetIds}
     * list, in group order. Set by the slot-by-slot trigger walker and by casts whose variable target
     * groups are separated explicitly, which lets a controller
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
     * Keywords the resolving permanent enters with that last until the start of its controller's
     * next turn (Jade Orb of Dragonkind's hexproof on a Dragon). Drained into the entering
     * {@code Permanent.untilNextTurnKeywords} by {@code StackResolutionService}.
     */
    private final Set<Keyword> untilNextTurnKeywordsOnEntry = EnumSet.noneOf(Keyword.class);
    /** Suspend grants haste for as long as the same player controls the resulting permanent. */
    @Setter private boolean suspendHasteOnEntry;
    /** Cast for a blitz cost granted by another source (Henzie): the permanent is sacrificed at the next end step. */
    @Setter private boolean grantedBlitzOnEntry;
    /** Colors granted to the permanent as this spell enters the battlefield. */
    private final Set<CardColor> grantedColorsOnEntry = EnumSet.noneOf(CardColor.class);
    /** Creature subtypes granted to the permanent as this spell enters the battlefield. */
    private final Set<CardSubtype> grantedSubtypesOnEntry = EnumSet.noneOf(CardSubtype.class);
    /** Base power/toughness overrides carried by this permanent spell onto the battlefield. */
    @Setter private Integer basePowerOverrideOnEntry;
    @Setter private Integer baseToughnessOverrideOnEntry;
    /** Keywords this spell gains while it is on the stack. */
    private final Set<Keyword> grantedKeywordsWhileOnStack = EnumSet.noneOf(Keyword.class);
    /**
     * Whether a static ability of a permanent its controller controls (Shadow the Hedgehog) gives this
     * spell split second because mana from an artifact was spent to cast it. The grant lasts only while
     * that permanent keeps the ability, so it is re-checked instead of being stored in
     * {@link #grantedKeywordsWhileOnStack}.
     */
    @Setter private boolean artifactManaSplitSecond;
    /**
     * Bloodthirst granted to this creature spell while it is on the stack (Bloodlord of Vaasgoth:
     * "it gains bloodthirst 3"). Per CR 702.54c each instance of bloodthirst applies separately, so
     * repeated grants accumulate. Stamped onto the entering {@code Permanent.grantedBloodthirst} by
     * {@code StackResolutionService} and turned into +1/+1 counters by the as-enters replacement.
     */
    @Setter private int grantedBloodthirst;
    /** Numeric devour grant carried by this creature spell while it is on the stack. */
    @Setter private int grantedDevour;
    /** Abilities granted until end of turn to the permanent as this spell enters the battlefield. */
    @Getter(AccessLevel.NONE)
    private final List<Ability> grantedAbilitiesOnEntry = new ArrayList<>();
    /** Abilities granted indefinitely to the permanent as this spell enters the battlefield. */
    @Getter(AccessLevel.NONE)
    private final List<Ability> persistentAbilitiesOnEntry = new ArrayList<>();
    /** Additional loyalty counters granted to a planeswalker spell before it enters. */
    @Setter private int grantedAdditionalLoyaltyCounters;
    /** Explicit starting loyalty for a copied planeswalker spell, when the copy defines one. */
    @Setter private Integer startingLoyalty;
    /**
     * Ids of permanents (tokens) created by effects earlier in <em>this</em> resolution. Populated
     * by the token-creation handlers and read back by a later effect on the same entry that acts on
     * "those tokens" — e.g. Gilt-Leaf Ambush grants deathtouch on a clash win to the tokens it just
     * created via {@code GrantScope.TOKENS_CREATED_THIS_RESOLUTION}.
     */
    private final List<UUID> createdPermanentIds = new ArrayList<>();

    /** Cards actually drawn and still identifiable during this entry's resolution. */
    private final List<UUID> drawnCardIdsThisResolution = new ArrayList<>();

    public void recordCardDrawnThisResolution(UUID cardId) {
        if (cardId != null && !drawnCardIdsThisResolution.contains(cardId)) {
            drawnCardIdsThisResolution.add(cardId);
        }
    }

    /**
     * Players this entry has actually dealt damage to while resolving, in order. Written by the damage
     * handlers and read back by a later effect on the same entry that acts on "each player dealt damage
     * this way" — e.g. Chandra, Roaring Flame's ultimate hands an emblem only to the opponents whose
     * damage was not fully prevented.
     */
    private final List<UUID> playersDealtDamageThisResolution = new ArrayList<>();

    /**
     * Permanents this entry has actually dealt damage to while resolving, in order, including damage a
     * redirect moved onto a different permanent. Read back by a later effect on the same entry that acts
     * on "if a [permanent] is dealt damage this way".
     */
    private final List<UUID> permanentsDealtDamageThisResolution = new ArrayList<>();

    /** Trigger effects already fired, keyed by the damage event of the effect that dealt the damage. */
    private final Map<UUID, Set<List<Object>>> noncombatExcessDamageTriggerEffectsFired = new HashMap<>();

    /** Records that this entry dealt damage to {@code playerId}; duplicates are ignored. */
    public void recordPlayerDealtDamage(UUID playerId) {
        if (playerId != null && !playersDealtDamageThisResolution.contains(playerId)) {
            playersDealtDamageThisResolution.add(playerId);
        }
    }

    /** Records that this entry dealt damage to {@code permanentId}; duplicates are ignored. */
    public void recordPermanentDealtDamage(UUID permanentId) {
        if (permanentId != null && !permanentsDealtDamageThisResolution.contains(permanentId)) {
            permanentsDealtDamageThisResolution.add(permanentId);
        }
    }

    public boolean markNoncombatExcessDamageTriggerFired(UUID sourcePermanentId, int abilityIndex) {
        return noncombatExcessDamageTriggerEffectsFired
                .computeIfAbsent(sourcePermanentId, ignored -> new HashSet<>())
                .add(Arrays.asList(getResolvingDamageEvent(), abilityIndex));
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
    /** The player who cast this spell, which stays the caster when another player gains control of it. */
    public UUID getCasterId() {
        return casterId != null ? casterId : controllerId;
    }

    /** Gives control of this spell to {@code newControllerId} while remembering who cast it. */
    public void gainControl(UUID newControllerId) {
        if (casterId == null) {
            casterId = controllerId;
        }
        controllerId = newControllerId;
    }

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
        this.damageAssignments = damageAssignments != null ? new LinkedHashMap<>(damageAssignments) : Map.of();
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

    // Zone-aware triggered ability with a source permanent and target (e.g. dynamic ward)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description,
                      List<CardEffect> effectsToResolve, UUID targetId, Zone targetZone,
                      UUID sourcePermanentId) {
        this.entryType = entryType;
        this.card = freezeCard(card);
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetId = targetId;
        this.sourcePermanentId = sourcePermanentId;
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
        this.damageAssignments = damageAssignments != null ? new LinkedHashMap<>(damageAssignments) : Map.of();
        this.targetZone = targetZone;
        this.targetCardIds = targetCardIds != null ? targetCardIds : List.of();
        this.targetFilter = null;
        boolean explicitTargetIds = targetIds != null && !targetIds.isEmpty();
        this.targetIds = explicitTargetIds ? targetIds : assignmentTargetIds(this.damageAssignments);
        this.targetIdsFromAssignments = !explicitTargetIds && !this.targetIds.isEmpty();
        this.primaryTargetStoredSeparately = targetId != null
                && explicitTargetIds
                && targetZone != Zone.GRAVEYARD;
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
        this.subgameResult = source.subgameResult;
        this.entryType = source.entryType;
        this.bombardmentOriginalEntryType = source.bombardmentOriginalEntryType;
        this.card = source.card;
        this.castCard = source.castCard;
        this.aftermathHalf = source.aftermathHalf;
        this.aftermathHalfSource = source.aftermathHalfSource;
        this.aftermathHalfFromGraveyard = source.aftermathHalfFromGraveyard;
        this.controllerId = source.controllerId;
        this.casterId = source.casterId;
        this.stackTimestamp = source.stackTimestamp;
        this.activePlayerId = source.activePlayerId;
        this.triggerRecordSettled = source.triggerRecordSettled;
        this.triggerRecordCarried = source.triggerRecordCarried;
        this.description = source.description;
        this.effectsToResolve = new ArrayList<>(source.effectsToResolve);
        this.cardCountsBeforeResolution = source.cardCountsBeforeResolution;
        this.bombardmentOriginalEffectsToResolve = source.bombardmentOriginalEffectsToResolve == null
                ? null : new ArrayList<>(source.bombardmentOriginalEffectsToResolve);
        this.resolvingEffectIndex = source.resolvingEffectIndex;
        this.simultaneousStepGroups.putAll(source.simultaneousStepGroups);
        this.nextSimultaneousGroup = source.nextSimultaneousGroup;
        this.xValue = source.xValue;
        this.xValueSetByCastTrigger = source.xValueSetByCastTrigger;
        this.modalModeCount = source.modalModeCount;
        this.phyrexianManaPaidWithLife = source.phyrexianManaPaidWithLife;
        this.etbMode = source.etbMode;
        this.targetId = source.targetId;
        this.opponentChosenTargetPlayerId = source.opponentChosenTargetPlayerId;
        this.sourcePermanentId = source.sourcePermanentId;
        this.resolutionSourcePermanentId = source.resolutionSourcePermanentId;
        this.damageAssignments = source.damageAssignments.isEmpty() ? Map.of() : new LinkedHashMap<>(source.damageAssignments);
        this.divisionAnnounced = source.divisionAnnounced;
        this.lastKnownPermanentCards.putAll(source.lastKnownPermanentCards);
        this.battlefieldChosenTargetCardIds.addAll(source.battlefieldChosenTargetCardIds);
        this.lastKnownPermanentPowers.putAll(source.lastKnownPermanentPowers);
        this.lastKnownPermanentToughnesses.putAll(source.lastKnownPermanentToughnesses);
        this.lastKnownPermanentAttachments.putAll(source.lastKnownPermanentAttachments);
        source.lastKnownPermanentCounters.forEach((id, counts) ->
                this.lastKnownPermanentCounters.put(id, new EnumMap<>(counts)));
        this.returnedPermanentIds.addAll(source.returnedPermanentIds);
        this.lastKnownTargetColors.putAll(source.lastKnownTargetColors);
        this.counters.putAll(source.counters);
        this.enteringCounters.putAll(source.enteringCounters);
        this.sourceStackCardId = source.sourceStackCardId;
        this.activationManaSpent = source.activationManaSpent.isEmpty() ? Map.of() : new HashMap<>(source.activationManaSpent);
        this.activationTreasureManaSpent = source.activationTreasureManaSpent;
        this.activationUsedTreasureMana = source.activationUsedTreasureMana;
        this.manaSpentToCast = source.manaSpentToCast;
        this.creatureManaSpentToCast = source.creatureManaSpentToCast;
        this.colorsSpentToCast = source.colorsSpentToCast;
        this.manaSpentByColorToCast = source.manaSpentByColorToCast;
        this.additionalEnterCounters = source.additionalEnterCounters;
        this.targetZone = source.targetZone;
        this.targetCardIds = source.targetCardIds.isEmpty() ? List.of() : new ArrayList<>(source.targetCardIds);
        this.targetCardGroupSizes = source.targetCardGroupSizes.isEmpty()
                ? List.of() : new ArrayList<>(source.targetCardGroupSizes);
        this.targetCardIdsByEffect = copyTargetCardIdsByEffect(source.targetCardIdsByEffect);
        this.targetFilter = source.targetFilter;
        this.removedPermanentControllers.putAll(source.removedPermanentControllers);
        this.requiredTargetControllerId = source.requiredTargetControllerId;
        this.targetControllersAtCast.putAll(source.targetControllersAtCast);
        this.copy = source.copy;
        this.castCopy = source.castCopy;
        this.nonTargeting = source.nonTargeting;
        this.reflexiveTrigger = source.reflexiveTrigger;
        this.spellDispositionHandled = source.spellDispositionHandled;
        this.returnToHandAfterResolving = source.returnToHandAfterResolving;
        this.putIntoLibraryPositionAfterResolving = source.putIntoLibraryPositionAfterResolving;
        this.spellMovedDuringResolution = source.spellMovedDuringResolution;
        this.castWithFlashback = source.castWithFlashback;
        this.castWithFlashforward = source.castWithFlashforward;
        this.castWithEscape = source.castWithEscape;
        this.castWithMutate = source.castWithMutate;
        this.escapeExiledCardIds = source.escapeExiledCardIds.isEmpty()
                ? List.of() : new ArrayList<>(source.escapeExiledCardIds);
        this.exileAndReturnToHandAtNextEndStep = source.exileAndReturnToHandAtNextEndStep;
        this.exileAndReturnSourceCard = source.exileAndReturnSourceCard;
        this.exileInsteadOfGraveyard = source.exileInsteadOfGraveyard;
        this.exilePermanentIfLeavesBattlefield = source.exilePermanentIfLeavesBattlefield;
        this.exileWithSourcePermanentId = source.exileWithSourcePermanentId;
        this.competingExileSourcePermanentIds.addAll(source.competingExileSourcePermanentIds);
        this.putOnBottomOfOwnersLibraryInsteadOfGraveyard =
                source.putOnBottomOfOwnersLibraryInsteadOfGraveyard;
        this.castWithDisturb = source.castWithDisturb;
        this.castWithOmen = source.castWithOmen;
        this.castWithAdventure = source.castWithAdventure;
        this.castWithWarp = source.castWithWarp;
        this.castTransformed = source.castTransformed;
        this.castFaceDown = source.castFaceDown;
        this.castWithDisguise = source.castWithDisguise;
        this.faceDownTurnsFaceUpOnDamageOrTap = source.faceDownTurnsFaceUpOnDamageOrTap;
        this.entersTapped = source.entersTapped;
        this.sourceZone = source.sourceZone;
        this.cyclingAbility = source.cyclingAbility;
        this.loyaltyAbility = source.loyaltyAbility;
        this.delvedCardIds = source.delvedCardIds.isEmpty() ? List.of() : new ArrayList<>(source.delvedCardIds);
        this.delvedCards = source.delvedCards.isEmpty() ? List.of() : new ArrayList<>(source.delvedCards);
        this.ownerIdOverride = source.ownerIdOverride;
        this.kicked = source.kicked;
        this.buyback = source.buyback;
        this.putCounterCostPaid = source.putCounterCostPaid;
        this.collectEvidenceCostPaid = source.collectEvidenceCostPaid;
        this.beholdCostPaid = source.beholdCostPaid;
        this.beholdPower = source.beholdPower;
        this.beholdPermanentId = source.beholdPermanentId;
        this.revealCardFromHandCostPaid = source.revealCardFromHandCostPaid;
        this.casualtyCostPaid = source.casualtyCostPaid;
        this.waterbendCostPaid = source.waterbendCostPaid;
        this.teamworkCostPaid = source.teamworkCostPaid;
        this.repeatedAdditionalCosts = source.repeatedAdditionalCosts.isEmpty()
                ? List.of() : new ArrayList<>(source.repeatedAdditionalCosts);
        this.castWhenSorceryCouldNotBeCast = source.castWhenSorceryCouldNotBeCast;
        this.castDuringMainPhase = source.castDuringMainPhase;
        this.evoked = source.evoked;
        this.bestowOriginalCard = source.bestowOriginalCard;
        this.physicalCard = source.physicalCard;
        this.prowl = source.prowl;
        this.spectacle = source.spectacle;
        this.sneak = source.sneak;
        this.castForForetell = source.castForForetell;
        this.foretellControllerTurnsAtExile = source.foretellControllerTurnsAtExile;
        this.foretellTurnsTakenAtExile = source.foretellTurnsTakenAtExile;
        this.alternateCost = source.alternateCost;
        this.webSlingingReturnedCreatureManaValue = source.webSlingingReturnedCreatureManaValue;
        this.overloaded = source.overloaded;
        this.controlledMountAsCast = source.controlledMountAsCast;
        this.controlledDragonAsCast = source.controlledDragonAsCast;
        this.controlledFaerieAsCast = source.controlledFaerieAsCast;
        this.controlledModifiedCreatureAsCast = source.controlledModifiedCreatureAsCast;
        this.lastManaPaymentPlayerId = source.lastManaPaymentPlayerId;
        this.cardsMilledAsActivationCost = List.copyOf(source.cardsMilledAsActivationCost);
        this.revealedPowerCostCard = source.revealedPowerCostCard;
        this.revealedPowerCostCardLastKnownPower = source.revealedPowerCostCardLastKnownPower;
        this.beheldCard = source.beheldCard;
        this.beheldCardOwnerId = source.beheldCardOwnerId;
        this.beholdChosenSubtype = source.beholdChosenSubtype;
        this.chosenCreatureType = source.chosenCreatureType;
        this.chosenCreatureTypes.putAll(source.chosenCreatureTypes);
        this.damageSourceCard = source.damageSourceCard;
        this.damageSourceControllerId = source.damageSourceControllerId;
        this.exilesCreaturesDamaged = source.exilesCreaturesDamaged;
        this.planarAbilityId = source.planarAbilityId;
        this.sourcePlanarObject = source.sourcePlanarObject == null ? null : source.sourcePlanarObject.copy();
        this.spellDamageContinuation = source.spellDamageContinuation;
        this.stateTriggerEffectIndex = source.stateTriggerEffectIndex;
        this.causedPileGroupingOrGuessThisTurn = source.causedPileGroupingOrGuessThisTurn;
        this.gainLifeEqualToGreatestPowerOfCardsPutIntoGraveyard = source.gainLifeEqualToGreatestPowerOfCardsPutIntoGraveyard;
        this.recordLibraryGraveyardCount = source.recordLibraryGraveyardCount;
        this.onceEachTurnActionKey = source.onceEachTurnActionKey;
        this.awaitingProducedManaColor = source.awaitingProducedManaColor;
        this.acceptedPlayerIds = source.acceptedPlayerIds;
        this.counteredPermanentIdsThisResolution.addAll(source.counteredPermanentIdsThisResolution);
        this.counteredSpellControllerId = source.counteredSpellControllerId;
        this.resolutionExiledCardIds = source.resolutionExiledCardIds;
        this.resolutionResults = source.resolutionResults;
        this.sourcePermanentSnapshot = source.sourcePermanentSnapshot;
        this.sourceAttachmentIds = source.sourceAttachmentIds;
        this.sacrificedAttachedEquipmentIds = source.sacrificedAttachedEquipmentIds.isEmpty()
                ? List.of() : new ArrayList<>(source.sacrificedAttachedEquipmentIds);
        this.chosenPermanentId = source.chosenPermanentId;
        this.chosenPermanentPowerAtLastKnown = source.chosenPermanentPowerAtLastKnown;
        this.chosenPermanentToughnessAtLastKnown = source.chosenPermanentToughnessAtLastKnown;
        this.returnedGraveyardCardId = source.returnedGraveyardCardId;
        this.returnedGraveyardCardPowerAtLastKnown = source.returnedGraveyardCardPowerAtLastKnown;
        this.returnedGraveyardCardToughnessAtLastKnown = source.returnedGraveyardCardToughnessAtLastKnown;
        this.chosenObjectCard = source.chosenObjectCard;
        this.searchedPermanentIds = source.searchedPermanentIds.isEmpty()
                ? List.of() : new ArrayList<>(source.searchedPermanentIds);
        this.triggerOrderChosen = source.triggerOrderChosen;
        this.mixedZoneTargetPermanentIds.putAll(source.mixedZoneTargetPermanentIds);
        this.mixedZoneTargetGraveyardVersions.putAll(source.mixedZoneTargetGraveyardVersions);
        this.targetCardGraveyardVersions.putAll(source.targetCardGraveyardVersions);
        this.targetGraveyardEntryVersion = source.targetGraveyardEntryVersion;
        this.sacrificedCardId = source.sacrificedCardId;
        this.sacrificedCardGraveyardVersions = source.sacrificedCardGraveyardVersions;
        this.sacrificedCardIds = source.sacrificedCardIds.isEmpty()
                ? List.of() : new ArrayList<>(source.sacrificedCardIds);
        this.sacrificedCardSnapshot = source.sacrificedCardSnapshot;
        this.discardedCardSnapshot = source.discardedCardSnapshot;
        this.sacrificedPermanentSnapshot = source.sacrificedPermanentSnapshot == null
                ? null : new Permanent(source.sacrificedPermanentSnapshot);
        this.sacrificedPower = source.sacrificedPower;
        this.sacrificedColorCount = source.sacrificedColorCount;
        this.sacrificedToughness = source.sacrificedToughness;
        this.sacrificedCard = source.sacrificedCard;
        this.exiledCostCardId = source.exiledCostCardId;
        this.exiledCostCardSnapshot = source.exiledCostCardSnapshot;
        this.activatedAbilityExiledCardIds = source.activatedAbilityExiledCardIds;
        this.convokeCreatureIds = source.convokeCreatureIds.isEmpty()
                ? List.of() : new ArrayList<>(source.convokeCreatureIds);
        this.convokeConniveCreatureIdsToProcess = source.convokeConniveCreatureIdsToProcess == null
                ? null : new ArrayList<>(source.convokeConniveCreatureIdsToProcess);
        this.targetConniveCreatureIdsToProcess = source.targetConniveCreatureIdsToProcess == null
                ? null : new ArrayList<>(source.targetConniveCreatureIdsToProcess);
        this.chosenCostPermanentIds = source.chosenCostPermanentIds.isEmpty()
                ? List.of() : new ArrayList<>(source.chosenCostPermanentIds);
        this.chosenCostPermanentSnapshots = source.chosenCostPermanentSnapshots.isEmpty()
                ? List.of() : source.chosenCostPermanentSnapshots.stream().map(Permanent::new).toList();
        this.targetIds = source.targetIds.isEmpty() ? List.of() : new ArrayList<>(source.targetIds);
        this.targetFilters = source.targetFilters.isEmpty() ? List.of() : new ArrayList<>(source.targetFilters);
        this.multiTargetConstraint = source.multiTargetConstraint;
        this.targetIdOverriddenForEffectResolution = source.targetIdOverriddenForEffectResolution;
        this.primaryTargetBeforeEffectResolution = source.primaryTargetBeforeEffectResolution;
        this.targetIdsFromAssignments = source.targetIdsFromAssignments;
        this.primaryTargetStoredSeparately = source.primaryTargetStoredSeparately;
        this.targetGroupSizes = source.targetGroupSizes.isEmpty()
                ? List.of() : new ArrayList<>(source.targetGroupSizes);
        this.illegalTargetIndices.addAll(source.illegalTargetIndices);
        this.grantedKeywordsOnEntry.addAll(source.grantedKeywordsOnEntry);
        this.untilNextTurnKeywordsOnEntry.addAll(source.untilNextTurnKeywordsOnEntry);
        this.suspendHasteOnEntry = source.suspendHasteOnEntry;
        this.grantedBlitzOnEntry = source.grantedBlitzOnEntry;
        this.grantedColorsOnEntry.addAll(source.grantedColorsOnEntry);
        this.grantedSubtypesOnEntry.addAll(source.grantedSubtypesOnEntry);
        this.basePowerOverrideOnEntry = source.basePowerOverrideOnEntry;
        this.baseToughnessOverrideOnEntry = source.baseToughnessOverrideOnEntry;
        this.grantedKeywordsWhileOnStack.addAll(source.grantedKeywordsWhileOnStack);
        this.grantedBloodthirst = source.grantedBloodthirst;
        this.grantedDevour = source.grantedDevour;
        this.grantedAbilitiesOnEntry.addAll(source.grantedAbilitiesOnEntry);
        this.persistentAbilitiesOnEntry.addAll(source.persistentAbilitiesOnEntry);
        this.grantedAdditionalLoyaltyCounters = source.grantedAdditionalLoyaltyCounters;
        this.startingLoyalty = source.startingLoyalty;
        this.drawnCardIdsThisResolution.addAll(source.drawnCardIdsThisResolution);
        source.noncombatExcessDamageTriggerEffectsFired.forEach((sourceId, effects) ->
                this.noncombatExcessDamageTriggerEffectsFired.put(sourceId, new HashSet<>(effects)));
        copyTriggerEventFrom(source);
    }

    /**
     * Copies what {@code source}'s trigger event was about onto this entry: the triggering permanent (with its
     * last-known snapshot and attachments), card, cards, spell, activated ability, player and vote, the permanent that
     * dealt the event's damage, the event's value, player and card payloads, the attack it was about, and the facts
     * the collector fixed from the event as the
     * ability triggered (the granting permanent, the linked exiled card, the source's graveyard incarnation and power).
     * The copy constructor copies every field through here, and a copy of an ability ({@code CopySupport}) copies
     * just these: a copy of a triggered ability is another instance of the same ability, so its "that creature", "its
     * counters" and "that player" mean the same objects as the original's (CR 707.10, CR 608.2h). A field that
     * records the trigger event belongs here, not in the copy constructor, so neither copy can drop it.
     */
    public void copyTriggerEventFrom(StackEntry source) {
        this.triggeringPermanentKeywordsAtLeave = source.triggeringPermanentKeywordsAtLeave == null
                ? null : EnumSet.copyOf(source.triggeringPermanentKeywordsAtLeave);
        this.eventDamageSource = source.eventDamageSource == null ? null : new Permanent(source.eventDamageSource);
        this.eventDamageSourceControllerId = source.eventDamageSourceControllerId;
        this.eventDamageSourceKeywordsAtLeave = source.eventDamageSourceKeywordsAtLeave == null
                ? null : EnumSet.copyOf(source.eventDamageSourceKeywordsAtLeave);
        this.triggeringCardExileEntryVersion = source.triggeringCardExileEntryVersion;
        this.attackedTargetId = source.attackedTargetId;
        this.attackingPermanentSnapshots = source.attackingPermanentSnapshots.stream().map(Permanent::new).toList();
        this.defendingPlayerId = source.defendingPlayerId;
        this.eventValue = source.eventValue;
        this.cardsLookedAtWhileScrying = source.cardsLookedAtWhileScrying;
        this.combatOpponentPowerAtTrigger = source.combatOpponentPowerAtTrigger;
        this.combatOpponentToughnessAtTrigger = source.combatOpponentToughnessAtTrigger;
        this.producedManaColors = source.producedManaColors;
        this.grantingPermanentId = source.grantingPermanentId;
        this.dyingPermanentManaValue = source.dyingPermanentManaValue;
        this.graveyardTargetMaxManaValue = source.graveyardTargetMaxManaValue;
        this.linkedExiledCardId = source.linkedExiledCardId;
        this.triggeringPermanentLastKnownCard = source.triggeringPermanentLastKnownCard;
        this.eventPlayerIds = source.eventPlayerIds.isEmpty() ? List.of() : new ArrayList<>(source.eventPlayerIds);
        this.eventNontokenPlayerIds = source.eventNontokenPlayerIds.isEmpty()
                ? List.of() : new ArrayList<>(source.eventNontokenPlayerIds);
        this.eventTargetedPlayerIds = source.eventTargetedPlayerIds.isEmpty()
                ? List.of() : new ArrayList<>(source.eventTargetedPlayerIds);
        this.eventDestroyedPlayerIds = source.eventDestroyedPlayerIds.isEmpty()
                ? List.of() : new ArrayList<>(source.eventDestroyedPlayerIds);
        this.eventCardIds = source.eventCardIds.isEmpty() ? List.of() : new ArrayList<>(source.eventCardIds);
        this.eventCardGraveyardEntryVersions.clear();
        this.eventCardGraveyardEntryVersions.putAll(source.eventCardGraveyardEntryVersions);
        this.eventManaValues = source.eventManaValues.isEmpty() ? List.of() : new ArrayList<>(source.eventManaValues);
        this.attachedPermanentSnapshot = source.attachedPermanentSnapshot;
        this.triggeringCardId = source.triggeringCardId;
        this.triggeringCardSnapshot = source.triggeringCardSnapshot;
        this.triggeringCardGraveyardEntryVersion = source.triggeringCardGraveyardEntryVersion;
        this.triggeringCards = source.triggeringCards;
        this.triggeringSpellTargetIds = source.triggeringSpellTargetIds;
        this.triggeringSpellSnapshot = source.triggeringSpellSnapshot == null
                ? null : new StackEntry(source.triggeringSpellSnapshot);
        this.triggeringActivatedAbility = source.triggeringActivatedAbility;
        this.triggeringSpellCastIndex = source.triggeringSpellCastIndex;
        this.triggeringSpellTurnCastIndex = source.triggeringSpellTurnCastIndex;
        this.sourceGraveyardEntryVersion = source.sourceGraveyardEntryVersion;
        this.triggeringPermanentSnapshot = source.triggeringPermanentSnapshot == null
                ? null : new Permanent(source.triggeringPermanentSnapshot);
        this.triggeringAttachments = source.triggeringAttachments;
        this.triggeringPermanentId = source.triggeringPermanentId;
        this.triggeringPermanentIds = source.triggeringPermanentIds;
        this.triggeringPlayerId = source.triggeringPlayerId;
        this.eventCounterType = source.eventCounterType;
        this.awaitingSourceGraveyardEntry = source.awaitingSourceGraveyardEntry;
        this.triggeringVotingResult = source.triggeringVotingResult;
        this.triggeringPermanentControllerId = source.triggeringPermanentControllerId;
        this.triggeringPermanentOwnerId = source.triggeringPermanentOwnerId;
        this.triggeringPermanentPowerAtTrigger = source.triggeringPermanentPowerAtTrigger;
        this.triggeringPermanentToughnessAtTrigger = source.triggeringPermanentToughnessAtTrigger;
        this.sourcePowerAtTrigger = source.sourcePowerAtTrigger;
    }

    /** Whether taking this entry's action uses up its ability's "do this only once each turn" (CR 603.2h). */
    public boolean marksOnceEachTurnAction() {
        return onceEachTurnActionKey != null;
    }

    /**
     * Gives this entry, one a choice made as its ability went on the stack built (a mode, CR 603.3c; a target, CR
     * 603.3d), what {@code record} holds: the entry its collector builds for an ability that needs no choice. The
     * trigger event ({@link #copyTriggerEventFrom}), the source as it triggered and how it was cast (CR 608.2h: its
     * alternative cost, whether it was kicked, the optional and repeated costs paid, a promised gift), the "do this
     * only once each turn" mark (CR 603.2h) and the event's X are the same whichever way the ability reaches the
     * stack. An entry of an ability with no target to choose also takes the record's non-targeting "that player" or
     * "that creature".
     */
    public void carryTriggerRecord(StackEntry record) {
        // The limit a graveyard target was chosen against is the choice's, as its X is.
        Integer chosenAgainstMaxManaValue = this.graveyardTargetMaxManaValue;
        copyTriggerEventFrom(record);
        if (this.graveyardTargetMaxManaValue == null) {
            this.graveyardTargetMaxManaValue = chosenAgainstMaxManaValue;
        }
        if (record.sourcePermanentSnapshot != null) {
            this.sourcePermanentSnapshot = new Permanent(record.sourcePermanentSnapshot);
        }
        this.alternateCost = record.alternateCost;
        // What was paid as the source was cast is the same for every ability of it, whatever the ability chooses.
        this.kicked |= record.kicked;
        this.spectacle |= record.spectacle;
        this.collectEvidenceCostPaid |= record.collectEvidenceCostPaid;
        this.waterbendCostPaid |= record.waterbendCostPaid;
        this.revealCardFromHandCostPaid |= record.revealCardFromHandCostPaid;
        this.controlledDragonAsCast |= record.controlledDragonAsCast;
        this.giftPromised |= record.giftPromised;
        if (this.sacrificedToughness == 0) {
            this.sacrificedToughness = record.sacrificedToughness;
        }
        if (this.sacrificedPermanentSnapshot == null && record.sacrificedPermanentSnapshot != null) {
            // The permanent sacrificed for the ability ("when you do") as it last existed.
            this.sacrificedPermanentSnapshot = new Permanent(record.sacrificedPermanentSnapshot);
            this.sacrificedPower = record.sacrificedPower;
            this.sacrificedColorCount = record.sacrificedColorCount;
        }
        if (this.repeatedAdditionalCosts == null || this.repeatedAdditionalCosts.isEmpty()) {
            this.repeatedAdditionalCosts = record.repeatedAdditionalCosts;
        }
        if (this.convokeCreatureIds.isEmpty()) {
            this.convokeCreatureIds = record.convokeCreatureIds;
        }
        this.onceEachTurnActionKey = record.onceEachTurnActionKey;
        if (this.xValue == 0) {
            // The X the event gave the ability (the number of attackers of an attack ability), unless the choice set one.
            this.xValue = record.xValue;
        }
        if (record.nonTargeting && this.targetId == null
                && (this.targetCardIds == null || this.targetCardIds.isEmpty())
                && (this.targetIds == null || this.targetIds.isEmpty())
                && !hasTargetToChoose()) {
            this.targetId = record.targetId;
            this.nonTargeting = true;
        }
        // The active player as the ability triggered (the player whose step it is) is the event's too.
        this.activePlayerId = record.activePlayerId;
        if (record.entryType == StackEntryType.TRIGGERED_ABILITY && record.copy) {
            // A copied ability (a copied Saga chapter) is a copy whichever way it reaches the stack.
            this.copy = true;
        }
        this.triggerRecordCarried = true;
    }

    /**
     * Whether one of this entry's effects has a target: an ability of "up to one target" whose controller chose none
     * has no target, and no "that player" or "that creature" in its place.
     */
    private boolean hasTargetToChoose() {
        return effectsToResolve.stream().anyMatch(effect -> {
            com.github.laxika.magicalvibes.model.effect.TargetSpec spec = effect.targetSpec();
            return spec.declaredTarget() != null && !spec.selfTargeting();
        });
    }

    /** Whether this entry was given its collector's record of the trigger event ({@link #carryTriggerRecord}). */
    public boolean hasCarriedTriggerRecord() {
        return triggerRecordCarried;
    }

    /**
     * Whether this entry's record of its trigger event is final: it reached the stack or a holding queue, as the entry
     * its collector built, with that entry's record ({@link #carryTriggerRecord}), or with no record waiting for it. {@code GameData} gives a triggered ability the record handed off for it once, as it
     * first arrives.
     */
    public boolean isTriggerRecordSettled() {
        return triggerRecordSettled;
    }

    /** Marks this entry's record of its trigger event final (see {@link #isTriggerRecordSettled}). */
    public void settleTriggerRecord() {
        this.triggerRecordSettled = true;
    }

    public void addGrantedTriggeredEffectOnEntry(EffectSlot slot, CardEffect effect) {
        grantedAbilitiesOnEntry.add(LegacySlots.ability(slot, effect));
    }

    /** The abilities the permanent this spell becomes gains until end of turn as it enters. */
    public List<Ability> getGrantedAbilitiesOnEntry() {
        return Collections.unmodifiableList(grantedAbilitiesOnEntry);
    }

    public void addPersistentTriggeredEffectOnEntry(EffectSlot slot, CardEffect effect) {
        addPersistentAbilityOnEntry(LegacySlots.ability(slot, effect));
    }

    /** Has the permanent this spell becomes gain {@code ability} indefinitely as it enters. */
    public void addPersistentAbilityOnEntry(Ability ability) {
        persistentAbilitiesOnEntry.add(ability);
    }

    /** The abilities the permanent this spell becomes gains indefinitely as it enters. */
    public List<Ability> getPersistentAbilitiesOnEntry() {
        return Collections.unmodifiableList(persistentAbilitiesOnEntry);
    }

    public int getCounterCount(CounterType counterType) {
        return counters.getOrDefault(counterType, 0);
    }

    public void setCounterCount(CounterType counterType, int count) {
        if (count <= 0) {
            counters.remove(counterType);
        } else {
            counters.put(counterType, count);
        }
    }

    public void setEnteringCounterCount(CounterType counterType, int count) {
        if (count <= 0) {
            enteringCounters.remove(counterType);
        } else {
            enteringCounters.put(counterType, count);
        }
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

    /**
     * Splices a SequenceEffect's steps after the effect at {@code index}, like
     * {@link #insertEffectsToResolve}. The steps of a simultaneous sequence become one damage event,
     * and so do the steps of any sequence spliced from inside one.
     */
    public void spliceSequenceSteps(int index, List<CardEffect> steps, boolean simultaneous) {
        Integer group = simultaneousStepGroups.get(effectsToResolve.get(index));
        if (group == null && simultaneous) {
            group = nextSimultaneousGroup++;
        }
        if (group != null) {
            for (CardEffect step : steps) {
                simultaneousStepGroups.put(step, group);
            }
        }
        insertEffectsToResolve(index + 1, steps);
    }

    /** The damage event of the effect being dispatched; see {@link #damageEventAt}. */
    public int getResolvingDamageEvent() {
        return damageEventAt(resolvingEffectIndex);
    }

    /**
     * Identifies the damage event of the effect at {@code index}: the steps of one simultaneous
     * sequence share an event, an effect the card adds simultaneously shares the event of the effect
     * before it ({@link Card#addSimultaneousEffects}), and every other effect is an event of its own.
     */
    public int damageEventAt(int index) {
        if (index < 0 || index >= effectsToResolve.size()) {
            return index;
        }
        Integer group = simultaneousStepGroups.get(effectsToResolve.get(index));
        if (group != null) {
            return -2 - group;
        }
        while (index > 0 && card != null && card.isSimultaneousWithPrevious(effectsToResolve.get(index))) {
            index--;
        }
        return index;
    }

    /** Whether the effect at {@code index} shares its damage event with another effect. */
    public boolean isInSimultaneousDamageEvent(int index) {
        int event = damageEventAt(index);
        return event != index || damageEventAt(index + 1) == event;
    }

    public void replaceEffectToResolve(int index, CardEffect effect) {
        Integer group = simultaneousStepGroups.get(effectsToResolve.get(index));
        if (group != null) {
            simultaneousStepGroups.put(effect, group);
        }
        List<CardEffect> updated = new ArrayList<>(effectsToResolve);
        updated.set(index, effect);
        effectsToResolve = updated;
    }

    public void replaceEffectsToResolve(List<CardEffect> effects) {
        effectsToResolve = List.copyOf(effects);
    }

    /** Records each player's card count as this spell begins to resolve ({@link #cardCountsBeforeResolution}). */
    public void setCardCountsBeforeResolution(Map<UUID, Integer> counts) {
        cardCountsBeforeResolution = counts == null ? null : Map.copyOf(counts);
    }

    /** Returns and clears {@link #cardCountsBeforeResolution}, {@code null} when none was taken. */
    public Map<UUID, Integer> takeCardCountsBeforeResolution() {
        Map<UUID, Integer> counts = cardCountsBeforeResolution;
        cardCountsBeforeResolution = null;
        return counts;
    }

    /**
     * Exchanges this stack object with a card from hand, making it a normal creature spell while
     * retaining state that belongs to the stack object itself (such as counters and effects on it).
     */
    public void exchangeWithCardAsCreatureSpell(Card replacement) {
        this.castCard = freezeCard(replacement);
        this.physicalCard = freezeCard(replacement);
        this.entryType = StackEntryType.CREATURE_SPELL;
        this.bombardmentOriginalEntryType = null;
        this.bombardmentOriginalEffectsToResolve = null;
        this.description = replacement.getName();
        this.effectsToResolve = List.copyOf(replacement.getCastingCostsAndSpellEffects());
        this.targetId = null;
        this.targetZone = null;
        this.targetIds = List.of();
        this.targetCardIds = List.of();
        this.targetFilters = List.of();
        this.targetFilter = null;
        this.multiTargetConstraint = null;
        this.targetGroupSizes = List.of();
        this.targetCardGroupSizes = List.of();
        this.targetIdsFromAssignments = false;
        this.primaryTargetStoredSeparately = false;
        this.illegalTargetIndices.clear();
        this.grantedColorsOnEntry.clear();
        this.grantedSubtypesOnEntry.clear();
        this.basePowerOverrideOnEntry = null;
        this.baseToughnessOverrideOnEntry = null;

        // Cast-time choices and replacement effects belong to the old spell, not the exchanged card.
        this.modalModeCount = null;
        this.phyrexianManaPaidWithLife = 0;
        this.etbMode = null;
        this.opponentChosenTargetPlayerId = null;
        this.requiredTargetControllerId = null;
        this.targetControllersAtCast.clear();
        this.targetIdOverriddenForEffectResolution = false;
        this.resolvingEffectTargetGroup = null;
        this.sourceStackCardId = null;
        this.activationManaSpent = Map.of();
        this.activationTreasureManaSpent = 0;
        this.activationUsedTreasureMana = false;
        this.manaSpentToCast = 0;
        this.creatureManaSpentToCast = 0;
        this.colorsSpentToCast = Set.of();
        this.manaSpentByColorToCast = Map.of();
        this.copy = false;
        this.castCopy = false;
        this.nonTargeting = false;
        this.kicked = false;
        this.giftPromised = false;
        this.castWithFlashback = false;
        this.castWithFlashforward = false;
        this.castWithEscape = false;
        this.castWithMutate = false;
        this.castWithAdventure = false;
        this.castWithOmen = false;
        this.castWithDisturb = false;
        this.castTransformed = false;
        this.castFaceDown = false;
        this.castWithDisguise = false;
        this.faceDownTurnsFaceUpOnDamageOrTap = false;
        this.entersTapped = false;
        this.cyclingAbility = false;
        this.loyaltyAbility = false;
        this.buyback = false;
        this.putCounterCostPaid = false;
        this.collectEvidenceCostPaid = false;
        this.beholdCostPaid = false;
        this.beholdPower = 0;
        this.beholdPermanentId = null;
        this.revealCardFromHandCostPaid = false;
        this.casualtyCostPaid = false;
        this.waterbendCostPaid = false;
        this.teamworkCostPaid = false;
        this.castWhenSorceryCouldNotBeCast = false;
        this.castDuringMainPhase = false;
        this.evoked = false;
        this.bestowOriginalCard = null;
        this.prowl = false;
        this.spectacle = false;
        this.sneak = false;
        this.castForForetell = false;
        this.alternateCost = false;
        this.webSlingingReturnedCreatureManaValue = null;
        this.madness = false;
        this.overloaded = false;
        this.controlledMountAsCast = false;
        this.controlledDragonAsCast = false;
        this.controlledFaerieAsCast = false;
        this.controlledModifiedCreatureAsCast = false;
        this.lastManaPaymentPlayerId = null;
        this.cardsMilledAsActivationCost = List.of();
        this.revealedPowerCostCard = null;
        this.revealedPowerCostCardLastKnownPower = 0;
        this.beheldCard = null;
        this.beheldCardOwnerId = null;
        this.beholdChosenSubtype = null;
        this.chosenCreatureType = null;
        this.damageSourceCard = null;
        this.damageSourceControllerId = null;
        this.exilesCreaturesDamaged = false;
        this.spellDamageContinuation = false;
        this.stateTriggerEffectIndex = -1;
        this.attackedTargetId = null;
        this.defendingPlayerId = null;
        this.triggeringCardExileEntryVersion = -1L;
        this.attackingPermanentSnapshots = List.of();
        this.ownerIdOverride = null;
        this.sourceZone = Zone.HAND;
        this.spellDispositionHandled = false;
        this.returnToHandAfterResolving = false;
        this.putIntoLibraryPositionAfterResolving = null;
        this.spellMovedDuringResolution = false;
        this.exileAndReturnToHandAtNextEndStep = false;
        this.exileAndReturnSourceCard = null;
        this.exileInsteadOfGraveyard = false;
        this.exilePermanentIfLeavesBattlefield = false;
        this.putOnBottomOfOwnersLibraryInsteadOfGraveyard = false;
    }

    /** Replaces this stack object's references to another stack object, including multi-target lists. */
    public boolean replaceTargetReferences(UUID oldTargetId, UUID newTargetId) {
        boolean replaced = false;
        if (oldTargetId != null && oldTargetId.equals(this.targetId)) {
            this.targetId = newTargetId;
            replaced = true;
        }
        if (oldTargetId != null && targetIds.stream().anyMatch(oldTargetId::equals)) {
            this.targetIds = targetIds.stream()
                    .map(targetId -> oldTargetId.equals(targetId) ? newTargetId : targetId)
                    .toList();
            this.illegalTargetIndices.clear();
            replaced = true;
        }
        return replaced;
    }

    public void replaceEffectsForBombardment(List<CardEffect> missileEffects) {
        if (bombardmentOriginalEffectsToResolve == null) {
            bombardmentOriginalEffectsToResolve = new ArrayList<>(effectsToResolve);
        }
        effectsToResolve = List.copyOf(missileEffects);
    }

    public void setEntryType(StackEntryType entryType) {
        this.entryType = entryType;
    }

    public void convertToBombardmentMissileSpell(List<CardEffect> missileEffects) {
        if (bombardmentOriginalEntryType == null) {
            bombardmentOriginalEntryType = entryType;
        }
        if (bombardmentOriginalEffectsToResolve == null) {
            bombardmentOriginalEffectsToResolve = new ArrayList<>(effectsToResolve);
        }
        entryType = StackEntryType.SORCERY_SPELL;
        effectsToResolve = List.copyOf(missileEffects);
        targetId = null;
        targetIds = List.of();
    }

    public void restoreBombardmentSpellState() {
        if (bombardmentOriginalEntryType != null) {
            entryType = bombardmentOriginalEntryType;
            bombardmentOriginalEntryType = null;
        }
        if (bombardmentOriginalEffectsToResolve != null) {
            effectsToResolve = bombardmentOriginalEffectsToResolve;
            bombardmentOriginalEffectsToResolve = null;
        }
    }

    /** Records the graveyard incarnation of a targeted card as it is chosen as a target. */
    public void recordTargetCardGraveyardVersion(UUID cardId, long version) {
        targetCardGraveyardVersions.put(cardId, version);
    }

    /** Whether the card is still the same graveyard object that was targeted (it has not left and returned). */
    public boolean isTargetCardSameGraveyardObject(UUID cardId, long currentVersion) {
        Long recorded = targetCardGraveyardVersions.get(cardId);
        return recorded == null || recorded == currentVersion;
    }

    public Map<UUID, Long> getTargetCardGraveyardVersions() {
        return targetCardGraveyardVersions;
    }

    /**
     * Returns the card to use as the damage source for protection and prevention checks.
     * Normally this is the same as {@link #getCard()}, but for equipment-granted abilities
     * like Blazing Torch the damage source is the equipment, not the equipped creature.
     */
    public Card getEffectiveDamageSourceCard() {
        if (damageSourceCard != null) return damageSourceCard;
        if (sourcePermanentSnapshot != null && sourcePermanentId != null
                && sourcePermanentId.equals(sourcePermanentSnapshot.getId())) {
            return sourcePermanentSnapshot.getCard();
        }
        // A spell cast as its Omen or Adventure face has only that face's characteristics (its colors
        // decide protection from the damage it deals).
        if (castWithOmen || castWithAdventure) {
            return getTargetingCard();
        }
        return getCard();
    }

    public Card getCard() {
        if (castCard != null) {
            return castCard;
        }
        if (card != null && card.isAftermathSplitCard()
                && entryType != StackEntryType.TRIGGERED_ABILITY && entryType != StackEntryType.ACTIVATED_ABILITY) {
            return aftermathHalf();
        }
        return card;
    }

    /**
     * The half of an aftermath card this spell was cast as: its second half when cast from a
     * graveyard, its first half otherwise. While on the stack only that half's characteristics
     * exist (CR 702.127a, CR 709.3b). Cast paths put the whole card on the stack, so the half is
     * derived here rather than at each of them.
     */
    private Card aftermathHalf() {
        if (aftermathHalfSource != card || aftermathHalfFromGraveyard != castWithFlashback) {
            Card half;
            if (castWithFlashback) {
                half = card.createRuntimeCopyWithFace(card.graveyardCastHalf());
            } else {
                half = card.createRuntimeCopy();
                half.applySplitHalf(card.getSplitHalves().getFirst());
            }
            aftermathHalf = freezeCard(half);
            aftermathHalfSource = card;
            aftermathHalfFromGraveyard = castWithFlashback;
        }
        return aftermathHalf;
    }

    /** Whether this stack object has the given printed or cast-time-granted keyword. */
    public boolean hasKeyword(Keyword keyword) {
        return getCard() != null && (getCard().getKeywords().contains(keyword)
                || grantedKeywordsWhileOnStack.contains(keyword));
    }

    public void setCastCard(Card castCard) {
        this.castCard = freezeCard(castCard);
    }

    public void setBestowOriginalCard(Card bestowOriginalCard) {
        this.bestowOriginalCard = freezeCard(bestowOriginalCard);
    }

    public void setPhysicalCard(Card physicalCard) {
        this.physicalCard = freezeCard(physicalCard);
    }

    public void setConvokeCreatureIds(List<UUID> convokeCreatureIds) {
        this.convokeCreatureIds = convokeCreatureIds == null ? List.of() : List.copyOf(convokeCreatureIds);
    }

    public void setConvokeConniveCreatureIdsToProcess(List<UUID> creatureIds) {
        this.convokeConniveCreatureIdsToProcess = creatureIds == null ? null : List.copyOf(creatureIds);
    }

    public void setTargetConniveCreatureIdsToProcess(List<UUID> creatureIds) {
        this.targetConniveCreatureIdsToProcess = creatureIds == null ? null : List.copyOf(creatureIds);
    }

    public void setChosenCostPermanentIds(List<UUID> chosenCostPermanentIds) {
        this.chosenCostPermanentIds = chosenCostPermanentIds == null
                ? List.of() : List.copyOf(chosenCostPermanentIds);
    }

    public void setChosenCostPermanentSnapshots(List<Permanent> chosenCostPermanentSnapshots) {
        this.chosenCostPermanentSnapshots = chosenCostPermanentSnapshots == null
                ? List.of() : chosenCostPermanentSnapshots.stream().map(Permanent::new).toList();
    }

    public void setAttackingPermanentSnapshots(List<Permanent> snapshots) {
        attackingPermanentSnapshots = snapshots == null ? List.of()
                : snapshots.stream().map(Permanent::new).toList();
    }

    public void setTargetCardIdsByEffect(Map<CardEffect, List<UUID>> targetCardIdsByEffect) {
        if (targetCardIdsByEffect == null || targetCardIdsByEffect.isEmpty()) {
            this.targetCardIdsByEffect = Map.of();
            return;
        }
        IdentityHashMap<CardEffect, List<UUID>> copy = new IdentityHashMap<>();
        targetCardIdsByEffect.forEach((effect, cardIds) -> copy.put(effect,
                cardIds == null ? List.of() : List.copyOf(cardIds)));
        this.targetCardIdsByEffect = Collections.unmodifiableMap(copy);
    }

    public List<UUID> getTargetCardIdsForEffect(CardEffect effect) {
        List<UUID> effectTargetCardIds = targetCardIdsByEffect.get(effect);
        if (effectTargetCardIds != null) {
            return effectTargetCardIds;
        }
        Card targeting = getTargetingCard();
        int group = targeting == null ? -1 : targeting.getEffectTargetIndex(effect);
        if (group >= 0 && targeting != null) {
            List<UUID> declaredTargetCardIds = targetCardIds == null ? List.of() : targetCardIds;
            List<TargetGroup> groups = targeting.getTargetGroups();
            int graveyardTargetOffset = 0;
            for (TargetGroup targetGroup : groups) {
                if (!(targetGroup.getFilter() instanceof GraveyardCardPredicateTargetFilter)) {
                    continue;
                }
                int groupSize = Math.min(wasKicked() ? targetGroup.getKickedMaxTargets() : targetGroup.getMaxTargets(),
                        declaredTargetCardIds.size() - graveyardTargetOffset);
                if (targetGroup.getIndex() == group) {
                    if (groupSize <= 0) {
                        return List.of();
                    }
                    return List.copyOf(declaredTargetCardIds.subList(
                            graveyardTargetOffset, graveyardTargetOffset + groupSize));
                }
                graveyardTargetOffset += Math.max(groupSize, 0);
            }
        }
        return targetCardIds == null ? List.of() : targetCardIds;
    }

    private static Map<CardEffect, List<UUID>> copyTargetCardIdsByEffect(
            Map<CardEffect, List<UUID>> source) {
        if (source.isEmpty()) {
            return Map.of();
        }
        IdentityHashMap<CardEffect, List<UUID>> copy = new IdentityHashMap<>();
        source.forEach((effect, cardIds) -> copy.put(effect,
                cardIds == null ? List.of() : List.copyOf(cardIds)));
        return Collections.unmodifiableMap(copy);
    }

    public void setSacrificedCard(Card sacrificedCard) {
        this.sacrificedCard = freezeCard(sacrificedCard);
    }

    public Card getPhysicalCard() {
        if (bestowOriginalCard != null) {
            return bestowOriginalCard;
        }
        if (physicalCard != null) {
            return physicalCard;
        }
        // A split half is the whole card again once it leaves the stack (CR 709.4).
        return card != null && card.getSplitWholeCard() != null ? card.getSplitWholeCard() : card;
    }

    /**
     * The player to whom this spell's card belongs for disposition purposes (graveyard/hand/library on
     * leaving the stack). An {@link #ownerIdOverride} set for a control-diverged cast (Sen Triplets) wins,
     * then the physical card's recorded owner (a spell cast from another player's hand, Mindclaw Shaman),
     * and otherwise the controller — correct for every normal cast, where owner == controller.
     */
    public UUID getOwnerId() {
        if (ownerIdOverride != null) {
            return ownerIdOverride;
        }
        Card physical = getPhysicalCard();
        UUID cardOwnerId = physical != null ? physical.getOwnerId() : null;
        return cardOwnerId != null ? cardOwnerId : controllerId;
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

    /**
     * Records the targets and division a triggered ability announces after it was put on the stack
     * but before any player receives priority (CR 601.2d, 603.3d), replacing the flat target list
     * with the assignment keys.
     *
     * @param assignments each chosen target mapped to its share of the divided amount
     */
    public void announceDamageAssignments(Map<UUID, Integer> assignments) {
        this.damageAssignments = new LinkedHashMap<>(assignments);
        this.targetIds = assignmentTargetIds(this.damageAssignments);
        this.targetIdsFromAssignments = !this.targetIds.isEmpty();
        this.illegalTargetIndices.clear();
        this.divisionAnnounced = true;
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
        UUID previousTarget = updated.set(targetIndex, targetId);
        if (damageAssignments.containsKey(previousTarget)) {
            int assignedDamage = damageAssignments.remove(previousTarget);
            damageAssignments.merge(targetId, assignedDamage, Integer::sum);
        }
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
    public Card getTargetingCard() {
        if (card == null) {
            return null;
        }
        if ((castWithOmen || castWithAdventure || castWithDisturb || castTransformed)
                && card.getBackFaceCard() != null) {
            return card.getBackFaceCard();
        }
        Card effectiveCard = getCard();
        return castWithFlashback ? effectiveCard.graveyardCastHalf() : effectiveCard;
    }

    /**
     * Returns the targets chosen for the given target group, resolved against this entry's
     * flat {@link #targetIds} list.
     *
     * <p>For spells, the flat list is sliced by the card's {@link TargetGroup} declarations in
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
     * <p>Some entries store the first target separately in {@link #targetId}: Auras keep their
     * enchant target there, and spells that target different zones keep their primary target there.
     * For those entries the flat list holds only the later groups' targets and slicing starts at
     * group 1.</p>
     */
    public List<UUID> targetsForGroup(int group) {
        UUID primaryTarget = targetIdOverriddenForEffectResolution
                ? primaryTargetBeforeEffectResolution : targetId;
        if (targetIdsFromAssignments) {
            // The flat list holds assignment keys, not this group's chosen targets; the card's
            // declared group targets the separately stored primary target.
            return primaryTarget != null ? List.of(primaryTarget) : List.of();
        }
        Card targeting = getTargetingCard();
        List<TargetGroup> groups = targeting == null ? List.of() : targeting.getTargetGroups();
        if (groups.isEmpty()) {
            return group >= 0 && group < targetIds.size() && !illegalTargetIndices.contains(group)
                    ? List.of(targetIds.get(group)) : List.of();
        }
        int firstFlatGroup = 0;
        if (targeting.isAura() || primaryTargetStoredSeparately) {
            if (group == 0) {
                return primaryTarget != null ? List.of(primaryTarget) : List.of();
            }
            firstFlatGroup = 1;
        }
        int consumed = 0;
        for (TargetGroup g : groups) {
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
                    : groups.size() == 1 ? targetIds.size()
                    : isKicked() ? g.getKickedMaxTargets() : g.getMaxTargets();
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

    public void setTargetIdForEffectResolution(UUID targetId) {
        if (!this.targetIdOverriddenForEffectResolution) {
            this.primaryTargetBeforeEffectResolution = this.targetId;
        }
        this.targetId = targetId;
        this.targetIdOverriddenForEffectResolution = true;
    }

    public void restoreTargetIdAfterEffectResolution(UUID targetId) {
        this.targetId = targetId;
        this.targetIdOverriddenForEffectResolution = false;
        this.primaryTargetBeforeEffectResolution = null;
    }

    public void setResolvingEffectTargetGroup(Integer targetGroup) {
        this.resolvingEffectTargetGroup = targetGroup;
    }

    /**
     * Returns the targets for the target group currently being resolved, or {@code null} when the
     * current effect is not associated with a target group.
     *
     * <p>This is also used when a target-bound wrapper resolves an inner effect: the inner effect
     * is not present in the card's effect-to-target map, but it still applies to the wrapper's
     * target group.</p>
     */
    public List<UUID> targetsForResolvingEffectGroup() {
        return resolvingEffectTargetGroup == null ? null : targetsForGroup(resolvingEffectTargetGroup);
    }

    /**
     * Whether any effect that will actually resolve on this entry is bound to the given target
     * group. A group with no surviving bound effect (a gated-out intervening-if trigger) consumed
     * no targets from the flat {@link #targetIds} list. Returns {@code true} when the card declares
     * no effect/group mapping, preserving legacy positional slicing for ordinary multi-target
     * spells and abilities (where every declared group is always populated).
     */
    public boolean isTargetGroupActive(int groupIndex) {
        Card targeting = getTargetingCard();
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
            if (targeting.isEffectBoundToTargetGroup(effect, groupIndex)) {
                return true;
            }
        }
        return false;
    }

    public void rememberBattlefieldChosenTargetCard(UUID cardId) {
        if (cardId != null) {
            battlefieldChosenTargetCardIds.add(cardId);
        }
    }

    /**
     * Whether the card was chosen as a battlefield target. Per CR 608.2b a target that is no longer
     * in the zone it was targeted in is illegal, so such a card must not be treated as a graveyard target.
     */
    public boolean isBattlefieldChosenTargetCard(UUID cardId) {
        return battlefieldChosenTargetCardIds.contains(cardId);
    }

    /**
     * Records {@code cardId} as the card the trigger event was about ("that card", "it"), with the graveyard
     * incarnation it had as the ability triggered ({@code -1} for none), for the effects that read it as they
     * resolve.
     */
    public void setTriggeringCard(UUID cardId, long graveyardEntryVersion) {
        this.triggeringCardId = cardId;
        this.triggeringCardGraveyardEntryVersion = graveyardEntryVersion;
    }

    /**
     * The amount of mana spent to cast this spell: {@link #manaSpentToCast}, but 0 for a copy that wasn't cast, as a copy
     * of a spell isn't cast and mana isn't an object it copies (CR 707.10).
     */
    public int manaSpentToCastThisSpell() {
        return copy && !castCopy ? 0 : manaSpentToCast;
    }

    /** The mana produced by creatures spent to cast this spell, 0 for a copy that wasn't cast (CR 707.10). */
    public int creatureManaSpentToCastThisSpell() {
        return copy && !castCopy ? 0 : creatureManaSpentToCast;
    }

    /** Records {@code colors} as the colors of mana spent to cast this spell; an immutable copy is kept. */
    /** The mana of each color spent to cast this spell ({@link #manaSpentByColorToCast}). */
    public Map<ManaColor, Integer> getManaSpentByColorToCast() {
        return manaSpentByColorToCast;
    }

    public void setManaSpentByColorToCast(Map<ManaColor, Integer> manaSpentByColor) {
        this.manaSpentByColorToCast = manaSpentByColor == null || manaSpentByColor.isEmpty()
                ? Map.of() : Map.copyOf(manaSpentByColor);
    }

    public void setColorsSpentToCast(Set<ManaColor> colors) {
        this.colorsSpentToCast = colors == null || colors.isEmpty() ? Set.of() : Set.copyOf(colors);
    }

    /**
     * Records {@code spell}, a copy of which is kept, as the spell the trigger event was about, as it was on the stack
     * when the ability triggered. A {@code null} spell clears it. The collector sets the triggering card id apart.
     */
    public void setTriggeringSpell(StackEntry spell) {
        this.triggeringSpellSnapshot = spell == null ? null : new StackEntry(spell);
    }

    /** Records {@code attachmentIds} as the Auras and Equipment attached to the source as it left the battlefield. */
    public void setSourceAttachmentIds(List<UUID> attachmentIds) {
        this.sourceAttachmentIds = attachmentIds == null ? List.of() : List.copyOf(attachmentIds);
    }

    /** Records {@code cards} as the cards the trigger event was about, for the effects that read them as they resolve. */
    public void setTriggeringCards(List<Card> cards) {
        this.triggeringCards = cards == null ? List.of() : List.copyOf(cards);
    }

    /**
     * The card a "whenever [a player] plays a card" event was about: the triggering spell's card as it was on the stack
     * when the ability triggered, else the first of {@link #getTriggeringCards()} (the land played); {@code null} when
     * the entry records neither.
     */
    public Card triggeringSpellOrCard() {
        if (triggeringSpellSnapshot != null) {
            return triggeringSpellSnapshot.getCard();
        }
        return triggeringCards.isEmpty() ? null : triggeringCards.getFirst();
    }

    /** The ids of {@link #getTriggeringCards()}, in event order. */
    public List<UUID> triggeringCardIds() {
        return triggeringCards.stream().map(Card::getId).toList();
    }

    /**
     * Records {@code lastKnown}, a copy of which is kept, as the permanent the trigger event was about, as it last
     * existed on the battlefield (CR 603.10a). A {@code null} permanent clears it.
     */
    public void setTriggeringPermanent(Permanent lastKnown) {
        setTriggeringPermanent(lastKnown, com.github.laxika.magicalvibes.model.rules.DepartedAttachments.NONE);
    }

    /**
     * Records {@code lastKnown}, a copy of which is kept, as the permanent the trigger event was about, with the Auras
     * and Equipment attached to it just before it left (CR 603.10a).
     */
    public void setTriggeringPermanent(Permanent lastKnown,
                                       com.github.laxika.magicalvibes.model.rules.DepartedAttachments attachments) {
        this.triggeringPermanentSnapshot = lastKnown == null ? null : new Permanent(lastKnown);
        this.triggeringAttachments = attachments == null
                ? com.github.laxika.magicalvibes.model.rules.DepartedAttachments.NONE : attachments;
    }

    /**
     * The counters the triggering permanent had as it last existed on the battlefield (CR 603.10a), by type in
     * counter-type order; empty when there is no triggering permanent.
     */
    public Map<CounterType, Integer> triggeringPermanentCounters() {
        if (triggeringPermanentSnapshot == null || triggeringPermanentSnapshot.getCounters().isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new EnumMap<>(triggeringPermanentSnapshot.getCounters()));
    }

    /**
     * The number of {@code counterType} counters the triggering permanent had as it last existed on the battlefield,
     * or of all its counters for a {@code null} type; 0 when there is no triggering permanent.
     */
    public int triggeringPermanentCounterCount(CounterType counterType) {
        if (triggeringPermanentSnapshot == null) {
            return 0;
        }
        return counterType == null ? triggeringPermanentSnapshot.getTotalCounterCount()
                : triggeringPermanentSnapshot.getCounterCount(counterType);
    }

    /**
     * The graveyard incarnation of the ability's source card as the ability triggered ("this card" of "if this card is
     * in your graveyard", "return this card"), or {@code -1} when none was recorded: the one the collector recorded,
     * else, when the source card is the triggering card ("when this dies", "when this is put into your graveyard"; the
     * physical card of a transformed source), the triggering card's.
     */
    public long sourceCardGraveyardEntryVersion() {
        if (sourceGraveyardEntryVersion >= 0) {
            return sourceGraveyardEntryVersion;
        }
        if (triggeringCardId == null) {
            return -1;
        }
        boolean sourceIsTriggeringCard = card != null && card.getId().equals(triggeringCardId)
                || sourcePermanentSnapshot != null && sourcePermanentSnapshot.getOriginalCard() != null
                && triggeringCardId.equals(sourcePermanentSnapshot.getOriginalCard().getId());
        return sourceIsTriggeringCard ? triggeringCardGraveyardEntryVersion : -1;
    }

    /**
     * Whether a card whose current graveyard incarnation is {@code currentGraveyardEntryVersion} is still the
     * triggering card the event named: always when no incarnation was recorded (CR 400.7).
     */
    public boolean isTriggeringCardIncarnation(long currentGraveyardEntryVersion) {
        return triggeringCardGraveyardEntryVersion < 0
                || triggeringCardGraveyardEntryVersion == currentGraveyardEntryVersion;
    }

    public void rememberLastKnownPermanentCard(UUID permanentId, Card card) {
        if (permanentId != null && card != null) {
            lastKnownPermanentCards.put(permanentId, card);
        }
    }

    public void rememberRemovedPermanentController(UUID permanentId, UUID controllerId) {
        if (permanentId != null && controllerId != null) {
            removedPermanentControllers.put(permanentId, controllerId);
        }
    }

    /** The controller {@code permanentId} had when it left the battlefield, if this entry recorded it. */
    public UUID lastKnownPermanentController(UUID permanentId) {
        return permanentId == null ? null : removedPermanentControllers.get(permanentId);
    }

    public Card lastKnownPermanentCard(UUID permanentId) {
        return lastKnownPermanentCards.get(permanentId);
    }

    /** Records {@code attachments} as what was attached to {@code permanentId} just before it left the battlefield. */
    public void rememberLastKnownPermanentAttachments(UUID permanentId,
            com.github.laxika.magicalvibes.model.rules.DepartedAttachments attachments) {
        if (permanentId != null && attachments != null) {
            lastKnownPermanentAttachments.put(permanentId, attachments);
        }
    }

    /**
     * The Auras and Equipment attached to {@code permanentId} just before it left the battlefield, as this entry
     * recorded them; {@link com.github.laxika.magicalvibes.model.rules.DepartedAttachments#NONE} when it recorded none
     * (the permanent hasn't left, or the entry doesn't refer to it).
     */
    public com.github.laxika.magicalvibes.model.rules.DepartedAttachments lastKnownPermanentAttachments(
            UUID permanentId) {
        return permanentId == null ? com.github.laxika.magicalvibes.model.rules.DepartedAttachments.NONE
                : lastKnownPermanentAttachments.getOrDefault(permanentId,
                        com.github.laxika.magicalvibes.model.rules.DepartedAttachments.NONE);
    }

    public void clearReturnedPermanentIds() {
        returnedPermanentIds.clear();
    }

    public void rememberReturnedPermanent(UUID permanentId) {
        if (permanentId != null) {
            returnedPermanentIds.add(permanentId);
        }
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
        Card targeting = getTargetingCard();
        int group = targeting == null ? -1
                : resolvingEffectTargetGroup != null && targeting.hasEffectTargetIndex(effect)
                ? resolvingEffectTargetGroup
                : targeting.getEffectTargetIndex(effect);
        if (group < 0) {
            return getTargetIds();
        }
        if (!primaryTargetStoredSeparately && targetZone == Zone.STACK && targetId != null
                && !targetIds.contains(targetId)
                && effect.targetSpec().admits(TargetPredicate.Kind.SPELL)) {
            return List.of(targetId);
        }
        if (targetIds.isEmpty()) {
            // A spell's lone target belongs to its first group; later groups were omitted.
            if (group != 0 && (entryType != StackEntryType.TRIGGERED_ABILITY
                    && entryType != StackEntryType.ACTIVATED_ABILITY
                    && targeting.getTargetGroups().size() > 1
                    || entryType == StackEntryType.ENCHANTMENT_SPELL && targeting.isAura())) {
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
        Card targeting = getTargetingCard();
        int group = targeting == null ? -1
                : resolvingEffectTargetGroup != null && targeting.hasEffectTargetIndex(effect)
                ? resolvingEffectTargetGroup
                : targeting.getEffectTargetIndex(effect);
        if (group < 0) {
            return null;
        }
        if (!primaryTargetStoredSeparately && targetZone == Zone.STACK && targetId != null
                && !targetIds.contains(targetId)
                && effect.targetSpec().admits(TargetPredicate.Kind.SPELL)) {
            return List.of(targetId);
        }
        if (targetIds.isEmpty()) {
            if (group != 0 && (entryType != StackEntryType.TRIGGERED_ABILITY
                    && entryType != StackEntryType.ACTIVATED_ABILITY
                    && targeting.getTargetGroups().size() > 1
                    || entryType == StackEntryType.ENCHANTMENT_SPELL && targeting.isAura())) {
                return List.of();
            }
            return targetId != null ? List.of(targetId) : List.of();
        }
        return targetsForGroup(group);
    }

    private static List<UUID> assignmentTargetIds(Map<UUID, Integer> assignments) {
        return assignments == null || assignments.isEmpty()
                ? List.of()
                : List.copyOf(assignments.keySet());
    }

    /**
     * True when this entry targets exactly one distinct object or player. A spell whose several target
     * slots all name the same permanent (Seeds of Strength) still targets only that one object.
     */
    public boolean isSingleTarget() {
        if (nonTargeting) {
            return false;
        }
        Set<UUID> distinctTargets = new HashSet<>(targetIds);
        distinctTargets.addAll(targetCardIds);
        if (targetId != null) {
            distinctTargets.add(targetId);
        }
        return distinctTargets.size() == 1;
    }

    public boolean hasAnyTarget() {
        return targetId != null || !targetIds.isEmpty() || !targetCardIds.isEmpty();
    }

    /**
     * This spell's mana value while it's on the stack: X counts once for each {X} in its mana cost and
     * not at all when the cost has no {X}, e.g. an X chosen by another effect (CR 202.3e).
     */
    public int getSpellManaValue() {
        if ((castWithAdventure || castWithOmen) && card != null && card.getBackFaceCard() != null) {
            return spellManaValue(card.getBackFaceCard(), xValue);
        }
        return spellManaValue(getCard(), xValue);
    }

    /** {@link #getSpellManaValue()} for a card whose stack entry is held separately. */
    public static int spellManaValue(Card card, int xValue) {
        if (card == null) {
            return 0;
        }
        int xSymbols = card.getParsedManaCost() == null ? 0 : card.getParsedManaCost().getXSymbolCount();
        return card.getManaValue() + xValue * xSymbols;
    }
}
