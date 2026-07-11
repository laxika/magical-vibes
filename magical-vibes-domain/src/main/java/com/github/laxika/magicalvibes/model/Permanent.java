package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.Setter;

import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class Permanent {

    private final UUID id;
    private Card card;
    private final Card originalCard;
    private boolean tapped;
    private boolean attacking;
    /** The UUID of the player or planeswalker this creature is attacking. Null when not attacking. */
    @Setter private UUID attackTarget;
    private boolean attackedThisTurn;
    private boolean blocking;
    private final List<Integer> blockingTargets = new ArrayList<>();
    private final List<UUID> blockingTargetIds = new ArrayList<>();
    private boolean summoningSick;
    @Setter private int powerModifier;
    @Setter private int toughnessModifier;
    @Setter private int damagePreventionShield;
    @Setter private int regenerationShield;
    @Setter private UUID attachedTo;
    @Setter private CardColor chosenColor;
    @Setter private String chosenName;
    @Setter private CardSubtype chosenSubtype;
    @Setter private ManaValueParity chosenManaValueParity;
    @Setter private UUID chosenPermanentId;
    @Setter private boolean cantBeBlocked;
    @Setter private boolean cantBlockThisTurn;
    @Setter private boolean mustAttackThisTurn;
    /** When non-null, this creature must attack this specific player (not their planeswalkers). Cleared at end of turn. */
    @Setter private UUID mustAttackTargetId;
    /** When true, at least one creature must block this creature this turn if able (e.g. Emergent Growth). Cleared at end of turn. */
    @Setter private boolean mustBeBlockedThisTurn;
    /** When true, all creatures able to block this creature this turn do so (Lure-style, one-shot, e.g. Alluring Scent). Cleared at end of turn. */
    @Setter private boolean mustBeBlockedByAllThisTurn;
    @Setter private boolean cantRegenerateThisTurn;
    /** If true, this creature is exiled instead of dying this turn (e.g. Red Sun's Zenith). Cleared at end of turn. */
    @Setter private boolean exileInsteadOfDieThisTurn;
    /** Secrets of Strixhaven "Prepared": true while this permanent is prepared (CR-style designation). While
     *  prepared, a copy of its prepare spell sits in exile (see {@link #preparedSpellCardId}) and the controller
     *  may cast it; casting it unprepares this permanent. */
    @Setter private boolean prepared;
    /** The id of the exiled prepare-spell copy linked to this permanent while it is prepared (null otherwise). */
    @Setter private UUID preparedSpellCardId;
    /** If true, this creature has "Whenever this creature deals damage to an opponent, you may return target creature
     *  that player controls to its owner's hand" until end of turn (e.g. Arm with Aether). Cleared at end of turn. */
    @Setter private boolean hasDamageToOpponentCreatureBounce;
    /** Triggered effects temporarily granted by one-shot effects until end of turn
     *  (e.g. Verdant Rebirth granting ON_DEATH → ReturnSourceCardFromGraveyardToOwnerHandEffect).
     *  Keyed by EffectSlot so the trigger collection system can look up effects for the relevant slot.
     *  Cleared every turn by {@link #resetModifiers()}. */
    private final Map<EffectSlot, List<CardEffect>> temporaryTriggeredEffects = new EnumMap<>(EffectSlot.class);
    @Setter private boolean basePowerToughnessOverriddenUntilEndOfTurn;
    @Setter private int basePowerOverride;
    @Setter private int baseToughnessOverride;
    @Setter private boolean animatedUntilEndOfTurn;
    /** When {@code true}, this permanent is animated as a creature until the combat phase ends
     *  (e.g. Jade Statue). Uses the same {@link #animatedPower}/{@link #animatedToughness}/
     *  {@link #animatedColor}/{@link #transientSubtypes} storage as {@link #animatedUntilEndOfTurn}.
     *  Cleared by {@link #clearCombatState()} when combat ends. */
    @Setter private boolean animatedUntilEndOfCombat;
    @Setter private int animatedPower;
    @Setter private int animatedToughness;
    @Setter private CardColor animatedColor;
    @Setter private boolean permanentlyAnimated;
    @Setter private int permanentAnimatedPower;
    @Setter private int permanentAnimatedToughness;
    /** All counters on this permanent, keyed by {@link CounterType}. Absent keys mean zero counters.
     *  New counter kinds require only a new {@link CounterType} value — never a new field here.
     *  Read/write via {@link #getCounterCount(CounterType)} / {@link #setCounterCount(CounterType, int)}. */
    private final Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
    @Setter private int loyaltyActivationsThisTurn;
    private final Set<Keyword> grantedKeywords = new HashSet<>();
    /** Keywords temporarily removed by one-shot effects (e.g. activated abilities).
     *  Cleared every turn by {@link #resetModifiers()}. */
    private final Set<Keyword> removedKeywords = new HashSet<>();
    /** Transient colors granted by static/continuous effects (equipment, auras, animation, etc.).
     *  Cleared every turn by {@link #resetModifiers()} and recomputed from active static effect sources.
     *  For persistent color grants from one-shot effects, see {@link #grantedColors}. */
    private final Set<CardColor> transientColors = EnumSet.noneOf(CardColor.class);
    /** When {@code true}, {@link #transientColors} completely replaces the permanent's natural color
     *  (e.g. "target creature becomes blue"). When {@code false}, transient colors are additive. */
    @Setter private boolean colorOverridden;
    /** Transient subtypes granted by static/continuous effects (equipment, auras, animation, etc.).
     *  Cleared every turn by {@link #resetModifiers()} and recomputed from active static effect sources.
     *  For persistent subtype grants from one-shot effects, see {@link #grantedSubtypes}. */
    private final List<CardSubtype> transientSubtypes = new ArrayList<>();
    /** When non-null, this land "becomes the basic land type of your choice" until end of turn,
     *  replacing its other land types and mana ability (e.g. Tideshaper Mystic). Distinct from
     *  {@link #transientSubtypes}, which is additive. Cleared every turn by {@link #resetModifiers()}. */
    @Setter private CardSubtype transientLandTypeOverride;
    /** When non-null, this creature "becomes a [creature type]" until end of turn, replacing all its
     *  other creature types (e.g. Boldwyr Intimidator: "target creature becomes a Coward"). Read by the
     *  layered pass, which strips every creature subtype and adds this one. Distinct from
     *  {@link #transientSubtypes} (additive) and {@link #transientLandTypeOverride} (land types).
     *  Cleared every turn by {@link #resetModifiers()}. */
    @Setter private CardSubtype transientCreatureTypeOverride;
    private final Set<CardType> grantedCardTypes = EnumSet.noneOf(CardType.class);
    /** Card types permanently granted by one-shot effects (e.g. Phyrexian Scriptures "becomes an artifact").
     *  NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  For transient card type grants from static/activated effects, see {@link #grantedCardTypes}. */
    private final Set<CardType> persistentGrantedCardTypes = EnumSet.noneOf(CardType.class);
    private final List<TextReplacement> textReplacements = new ArrayList<>();
    private final Set<CardType> protectionFromCardTypes = EnumSet.noneOf(CardType.class);
    private final Set<CardColor> protectionFromColorsUntilEndOfTurn = EnumSet.noneOf(CardColor.class);
    /** Subtypes for "protection from non-[subtype] creatures" granted until end of turn.
     *  If this set contains HUMAN, the permanent has "protection from non-Human creatures."
     *  Cleared by {@link #resetModifiers()}. */
    private final Set<CardSubtype> protectionFromNonSubtypeCreaturesUntilEndOfTurn = EnumSet.noneOf(CardSubtype.class);
    /** Blocking restrictions granted until end of turn by one-shot effects (e.g. Dread Charge:
     *  "black creatures you control can't be blocked this turn except by black creatures").
     *  Each entry means this creature can be blocked only by blockers matching the restriction's
     *  filter. Consumed by {@code GameQueryService.getBlockRestriction}; cleared by {@link #resetModifiers()}. */
    private final List<CanBeBlockedOnlyByFilterEffect> blockRestrictionsUntilEndOfTurn = new ArrayList<>();
    private final Set<UUID> cantBlockIds = new HashSet<>();
    private final Set<UUID> mustBlockIds = new HashSet<>();
    /** If true, this permanent is exiled instead of going to any other zone when it leaves the battlefield (CR 614.6). */
    @Setter private boolean exileIfLeavesBattlefield;
    /** When this permanent entered the battlefield from a graveyard, the ID of the player whose graveyard
     *  it came from; {@code null} otherwise. Read during the entering creature's ETB processing to fire
     *  "whenever a creature enters from your graveyard" triggers (e.g. Flayer of the Hatebound). */
    @Setter private UUID enteredFromGraveyardOwnerId;
    /** Source permanent IDs that prevent this permanent from untapping during its controller's untap step.
     *  Each entry means: "this permanent doesn't untap for as long as that source permanent remains tapped." */
    private final Set<UUID> untapPreventedByPermanentIds = new HashSet<>();
    /** Source permanent IDs that prevent this permanent from untapping during its controller's untap step.
     *  Each entry means: "this permanent doesn't untap for as long as that source permanent is on the battlefield."
     *  Unlike {@link #untapPreventedByPermanentIds}, the tapped state of the source does not matter. */
    private final Set<UUID> untapPreventedWhileSourceOnBattlefieldIds = new HashSet<>();
    /** Number of untap steps this permanent should skip. Decremented each untap step.
     *  Multiple triggers (e.g. land tapped twice while Vorinclex is out) stack independently.
     *  Used by Vorinclex, Voice of Hunger's opponent-land lock. */
    @Setter private int skipUntapCount;
    /** Accumulated damage marked on this creature (CR 704.5g). Reset during cleanup step. */
    @Setter private int markedDamage;
    /** Colors permanently granted by one-shot effects (e.g. Rise from the Grave "in addition to its other colors").
     *  NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  For transient color grants from static effects, see {@link #transientColors}. */
    private final Set<CardColor> grantedColors = EnumSet.noneOf(CardColor.class);
    /** Subtypes permanently granted by one-shot effects (e.g. Rise from the Grave "in addition to its other types").
     *  NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  For transient subtype grants from static effects, see {@link #transientSubtypes}. */
    private final List<CardSubtype> grantedSubtypes = new ArrayList<>();
    /** When true, the creature's base power has been permanently overridden (e.g. by an exchange effect
     *  like Evra, Halcyon Witness). NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  Participates in CR 613.7 layer-7b ordering via {@link #permanentBasePowerOverrideTimestamp}. */
    @Setter private boolean basePowerOverriddenPermanently;
    @Setter private int permanentBasePowerOverride;
    /** CR 613.7 timestamp of the exchange that set {@link #basePowerOverriddenPermanently} — the
     *  layered 7b pass orders the override against other base-P/T setters with it. */
    @Setter private long permanentBasePowerOverrideTimestamp;
    /** When true, the creature's base toughness has been permanently overridden (e.g. by an exchange effect
     *  like Tree of Redemption). NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  Participates in CR 613.7 layer-7b ordering via {@link #permanentBaseToughnessOverrideTimestamp}. */
    @Setter private boolean baseToughnessOverriddenPermanently;
    @Setter private int permanentBaseToughnessOverride;
    /** CR 613.7 timestamp of the exchange that set {@link #baseToughnessOverriddenPermanently}. */
    @Setter private long permanentBaseToughnessOverrideTimestamp;
    @Setter private boolean transformed;
    /** When true, this permanent has lost all abilities until end of turn (e.g. Merfolk Trickster).
     *  Keywords, activated abilities, and triggered abilities are suppressed.
     *  Cleared by {@link #resetModifiers()}. */
    @Setter private boolean losesAllAbilitiesUntilEndOfTurn;
    /** When true, this permanent has lost all creature types until end of turn (e.g. Amoeboid Changeling).
     *  All creature subtypes (base, transient, granted) are treated as absent, and the Changeling keyword
     *  no longer grants any creature types. Cleared by {@link #resetModifiers()}. */
    @Setter private boolean losesAllCreatureTypesUntilEndOfTurn;
    /** Whether this permanent was kicked when cast (tracked for "if wasn't kicked" triggers). */
    @Setter private boolean kicked;
    /** Whether this permanent was cast for its evoke cost (gates the evoke sacrifice ETB trigger). */
    @Setter private boolean evoked;
    /** Whether this permanent was cast for its prowl cost (gates "if its prowl cost was paid" ETB triggers). */
    @Setter private boolean prowl;
    /** Activated abilities temporarily granted by one-shot effects until end of turn
     *  (e.g. Navigator's Compass adding a basic land mana ability to a land).
     *  Cleared every turn by {@link #resetModifiers()}. */
    private final List<ActivatedAbility> temporaryActivatedAbilities = new ArrayList<>();
    /** Activated abilities granted for as long as this permanent remains on the battlefield
     *  (e.g. Aquitect's Will making a land an Island in addition to its other types — the
     *  granted "{T}: Add {U}" has no duration). Stored on the permanent rather than mutating
     *  the {@link Card}: Card instances are shared with AI simulation copies (see the copy
     *  constructor) and must stay immutable after construction.
     *  NOT cleared by {@link #resetModifiers()}. */
    private final List<ActivatedAbility> persistentGrantedActivatedAbilities = new ArrayList<>();
    /** When true, this permanent is a temporary copy (until end of turn) of another creature.
     *  At the cleanup step, the copy's floating layer-1 effect expires and the card reverts to
     *  {@link #preCopyCard} via {@link #revertEndOfTurnCopy()}.
     *  Used by Tilonalli's Skinshifter and similar shapeshifters. */
    @Setter private boolean copyUntilEndOfTurn;
    /** The card to revert to when the temporary copy effect ends.
     *  Only non-null when {@link #copyUntilEndOfTurn} is true. */
    @Setter private Card preCopyCard;
    /** Activated abilities granted until the controller's next turn begins
     *  (e.g. Song of Freyalise "{T}: Add one mana of any color.").
     *  NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Cleared at the beginning of the controller's next turn by
     *  {@link com.github.laxika.magicalvibes.model.Permanent#clearUntilNextTurnEffects()}. */
    private final List<ActivatedAbility> untilNextTurnActivatedAbilities = new ArrayList<>();
    /** When true, this land permanent is animated as a creature until the controller's next turn.
     *  NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Cleared at the beginning of the controller's next turn by
     *  {@link #clearUntilNextTurnEffects()}. */
    @Setter private boolean animatedUntilNextTurn;
    @Setter private int untilNextTurnAnimatedPower;
    @Setter private int untilNextTurnAnimatedToughness;
    /** Subtypes granted by "until your next turn" animation effects (e.g. Elemental from Sylvan Awakening).
     *  NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Cleared at the beginning of the controller's next turn by {@link #clearUntilNextTurnEffects()}. */
    private final List<CardSubtype> untilNextTurnSubtypes = new ArrayList<>();
    /** Keywords granted by "until your next turn" animation effects (e.g. reach, indestructible, haste from Sylvan Awakening).
     *  NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Cleared at the beginning of the controller's next turn by {@link #clearUntilNextTurnEffects()}. */
    private final Set<Keyword> untilNextTurnKeywords = new HashSet<>();
    /** When true, this permanent is a copy of another creature until {@link #copyUntilNextTurnControllerId}'s
     *  next turn (e.g. Shapesharer). NOT cleared by {@link #resetModifiers()} — survives end-of-turn cleanup.
     *  Reverts to {@link #untilNextTurnPreCopyCard} via {@link #revertUntilNextTurnCopy()} at the beginning
     *  of that player's turn. */
    @Setter private boolean copyUntilControllerNextTurn;
    /** The card to revert to when an "until your next turn" copy ends.
     *  Only non-null when {@link #copyUntilControllerNextTurn} is true. */
    @Setter private Card untilNextTurnPreCopyCard;
    /** The player whose next turn ends an "until your next turn" copy (the ability's controller). */
    @Setter private UUID copyUntilNextTurnControllerId;
    /** CR 613.7 timestamp: stamped from {@link GameData#nextTimestamp()} when this permanent
     *  enters a battlefield, and re-stamped each time it becomes attached (CR 613.7e — Auras and
     *  Equipment). Control changes do NOT re-stamp (CR 613.7c). Stays 0 for permanents added to
     *  a battlefield directly in tests; the layered engine falls back to battlefield position
     *  order for equal timestamps (see {@code agent-docs/LAYER_SYSTEM.md}). */
    @Setter private long timestamp;

    public Permanent(Card card) {
        this.id = UUID.randomUUID();
        // A card wrapped in a Permanent is live game state shared with AI simulation copies —
        // freeze it so any later mutation of the Card object fails fast instead of leaking.
        card.freeze();
        this.card = card;
        this.originalCard = card;
        this.tapped = false;
        this.attackedThisTurn = false;
        this.summoningSick = true;
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     * Preserves the same ID so that target references remain valid.
     * Card references are shared (immutable after construction).
     */
    public Permanent(Permanent source) {
        this.id = source.id;
        this.card = source.card;
        this.originalCard = source.originalCard;
        this.tapped = source.tapped;
        this.attacking = source.attacking;
        this.attackTarget = source.attackTarget;
        this.attackedThisTurn = source.attackedThisTurn;
        this.blocking = source.blocking;
        this.blockingTargets.addAll(source.blockingTargets);
        this.blockingTargetIds.addAll(source.blockingTargetIds);
        this.summoningSick = source.summoningSick;
        this.powerModifier = source.powerModifier;
        this.toughnessModifier = source.toughnessModifier;
        this.damagePreventionShield = source.damagePreventionShield;
        this.regenerationShield = source.regenerationShield;
        this.attachedTo = source.attachedTo;
        this.chosenColor = source.chosenColor;
        this.chosenName = source.chosenName;
        this.chosenSubtype = source.chosenSubtype;
        this.chosenManaValueParity = source.chosenManaValueParity;
        this.chosenPermanentId = source.chosenPermanentId;
        this.cantBeBlocked = source.cantBeBlocked;
        this.cantBlockThisTurn = source.cantBlockThisTurn;
        this.mustAttackThisTurn = source.mustAttackThisTurn;
        this.mustAttackTargetId = source.mustAttackTargetId;
        this.mustBeBlockedThisTurn = source.mustBeBlockedThisTurn;
        this.mustBeBlockedByAllThisTurn = source.mustBeBlockedByAllThisTurn;
        this.cantRegenerateThisTurn = source.cantRegenerateThisTurn;
        this.exileInsteadOfDieThisTurn = source.exileInsteadOfDieThisTurn;
        this.prepared = source.prepared;
        this.preparedSpellCardId = source.preparedSpellCardId;
        this.hasDamageToOpponentCreatureBounce = source.hasDamageToOpponentCreatureBounce;
        source.temporaryTriggeredEffects.forEach((slot, effects) ->
                this.temporaryTriggeredEffects.put(slot, new ArrayList<>(effects)));
        this.basePowerToughnessOverriddenUntilEndOfTurn = source.basePowerToughnessOverriddenUntilEndOfTurn;
        this.basePowerOverride = source.basePowerOverride;
        this.baseToughnessOverride = source.baseToughnessOverride;
        this.animatedUntilEndOfTurn = source.animatedUntilEndOfTurn;
        this.animatedUntilEndOfCombat = source.animatedUntilEndOfCombat;
        this.animatedPower = source.animatedPower;
        this.animatedToughness = source.animatedToughness;
        this.animatedColor = source.animatedColor;
        this.permanentlyAnimated = source.permanentlyAnimated;
        this.permanentAnimatedPower = source.permanentAnimatedPower;
        this.permanentAnimatedToughness = source.permanentAnimatedToughness;
        this.counters.putAll(source.counters);
        this.loyaltyActivationsThisTurn = source.loyaltyActivationsThisTurn;
        this.enteredFromGraveyardOwnerId = source.enteredFromGraveyardOwnerId;
        this.grantedKeywords.addAll(source.grantedKeywords);
        this.removedKeywords.addAll(source.removedKeywords);
        this.transientColors.addAll(source.transientColors);
        this.colorOverridden = source.colorOverridden;
        this.transientSubtypes.addAll(source.transientSubtypes);
        this.transientCreatureTypeOverride = source.transientCreatureTypeOverride;
        this.grantedCardTypes.addAll(source.grantedCardTypes);
        this.persistentGrantedCardTypes.addAll(source.persistentGrantedCardTypes);
        this.textReplacements.addAll(source.textReplacements);
        this.protectionFromCardTypes.addAll(source.protectionFromCardTypes);
        this.protectionFromColorsUntilEndOfTurn.addAll(source.protectionFromColorsUntilEndOfTurn);
        this.protectionFromNonSubtypeCreaturesUntilEndOfTurn.addAll(source.protectionFromNonSubtypeCreaturesUntilEndOfTurn);
        this.blockRestrictionsUntilEndOfTurn.addAll(source.blockRestrictionsUntilEndOfTurn);
        this.exileIfLeavesBattlefield = source.exileIfLeavesBattlefield;
        this.cantBlockIds.addAll(source.cantBlockIds);
        this.mustBlockIds.addAll(source.mustBlockIds);
        this.untapPreventedByPermanentIds.addAll(source.untapPreventedByPermanentIds);
        this.untapPreventedWhileSourceOnBattlefieldIds.addAll(source.untapPreventedWhileSourceOnBattlefieldIds);
        this.skipUntapCount = source.skipUntapCount;
        this.markedDamage = source.markedDamage;
        this.grantedColors.addAll(source.grantedColors);
        this.grantedSubtypes.addAll(source.grantedSubtypes);
        this.basePowerOverriddenPermanently = source.basePowerOverriddenPermanently;
        this.permanentBasePowerOverride = source.permanentBasePowerOverride;
        this.permanentBasePowerOverrideTimestamp = source.permanentBasePowerOverrideTimestamp;
        this.baseToughnessOverriddenPermanently = source.baseToughnessOverriddenPermanently;
        this.permanentBaseToughnessOverride = source.permanentBaseToughnessOverride;
        this.permanentBaseToughnessOverrideTimestamp = source.permanentBaseToughnessOverrideTimestamp;
        this.transformed = source.transformed;
        this.losesAllAbilitiesUntilEndOfTurn = source.losesAllAbilitiesUntilEndOfTurn;
        this.losesAllCreatureTypesUntilEndOfTurn = source.losesAllCreatureTypesUntilEndOfTurn;
        this.kicked = source.kicked;
        this.evoked = source.evoked;
        this.prowl = source.prowl;
        this.temporaryActivatedAbilities.addAll(source.temporaryActivatedAbilities);
        this.persistentGrantedActivatedAbilities.addAll(source.persistentGrantedActivatedAbilities);
        this.copyUntilEndOfTurn = source.copyUntilEndOfTurn;
        this.preCopyCard = source.preCopyCard;
        this.untilNextTurnActivatedAbilities.addAll(source.untilNextTurnActivatedAbilities);
        this.animatedUntilNextTurn = source.animatedUntilNextTurn;
        this.untilNextTurnAnimatedPower = source.untilNextTurnAnimatedPower;
        this.untilNextTurnAnimatedToughness = source.untilNextTurnAnimatedToughness;
        this.untilNextTurnSubtypes.addAll(source.untilNextTurnSubtypes);
        this.untilNextTurnKeywords.addAll(source.untilNextTurnKeywords);
        this.copyUntilControllerNextTurn = source.copyUntilControllerNextTurn;
        this.untilNextTurnPreCopyCard = source.untilNextTurnPreCopyCard;
        this.copyUntilNextTurnControllerId = source.copyUntilNextTurnControllerId;
        this.timestamp = source.timestamp;
    }

    public Card getOriginalCard() {
        return originalCard;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public void tap() {
        this.tapped = true;
    }

    public void untap() {
        // Stun counters (CR 122.1c / 701.x): if a tapped permanent would become untapped,
        // remove a stun counter from it instead. This is the single funnel point for all
        // untap sources (untap step, Seedborn Muse, "untap target", etc.).
        if (this.tapped && getCounterCount(CounterType.STUN) > 0) {
            setCounterCount(CounterType.STUN, getCounterCount(CounterType.STUN) - 1);
            return;
        }
        this.tapped = false;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
        if (attacking) {
            this.attackedThisTurn = true;
        }
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public void addBlockingTarget(int blockingTarget) {
        this.blockingTargets.add(blockingTarget);
    }

    public void addBlockingTargetId(UUID permanentId) {
        this.blockingTargetIds.add(permanentId);
    }

    public void setSummoningSick(boolean summoningSick) {
        this.summoningSick = summoningSick;
    }

    public boolean isAttached() {
        return attachedTo != null;
    }

    public void clearCombatState() {
        this.attacking = false;
        this.attackTarget = null;
        this.blocking = false;
        this.blockingTargets.clear();
        this.blockingTargetIds.clear();
        clearUntilEndOfCombatAnimation();
    }

    /**
     * Reverts an "until end of combat" animation (e.g. Jade Statue) when the combat phase ends.
     * Clears the shared animation storage that {@link #animatedUntilEndOfCombat} uses. No-op when
     * the permanent is not animated until end of combat, so unrelated transient grants are preserved.
     */
    public void clearUntilEndOfCombatAnimation() {
        if (!animatedUntilEndOfCombat) {
            return;
        }
        this.animatedUntilEndOfCombat = false;
        this.animatedPower = 0;
        this.animatedToughness = 0;
        this.animatedColor = null;
        this.grantedKeywords.clear();
        this.transientSubtypes.clear();
        this.grantedCardTypes.clear();
    }

    public void setAttackedThisTurn(boolean attackedThisTurn) {
        this.attackedThisTurn = attackedThisTurn;
    }

    /**
     * Returns the number of counters of the given concrete type on this permanent.
     * {@code ANY} and {@code SILVER} are category/wildcard types, not concrete counters
     * stored on a permanent, and are rejected.
     */
    public int getCounterCount(CounterType counterType) {
        if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
            throw new IllegalArgumentException(
                    "Counter type " + counterType + " is not a concrete permanent counter");
        }
        return counters.getOrDefault(counterType, 0);
    }

    public int getPlusOnePlusOneCounters() {
        return getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
    }

    /**
     * Sets the number of counters of the given concrete type on this permanent. A count of
     * zero (or less) removes the entry so the backing map only holds present counters.
     * See {@link #getCounterCount(CounterType)} for the supported types.
     */
    public void setCounterCount(CounterType counterType, int count) {
        if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
            throw new IllegalArgumentException(
                    "Counter type " + counterType + " is not a concrete permanent counter");
        }
        if (count <= 0) {
            counters.remove(counterType);
        } else {
            counters.put(counterType, count);
        }
    }

    /**
     * Returns only the modifier portion of power (counters + temporary modifiers),
     * without the base power. Used by static base P/T override effects (e.g. Deep Freeze)
     * that replace the base but preserve modifiers on top.
     */
    public int getPowerModifiers() {
        return powerModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
    }

    /**
     * Returns only the modifier portion of toughness (counters + temporary modifiers),
     * without the base toughness. Used by static base P/T override effects (e.g. Deep Freeze)
     * that replace the base but preserve modifiers on top.
     */
    public int getToughnessModifiers() {
        return toughnessModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
    }

    /**
     * This permanent's power from its own stored state (base + modifiers + counters), WITHOUT
     * layer-7d switches: P/T switching lives in floating continuous effects applied by the
     * layered pass ({@code GameQueryService.getEffectivePower} swaps the finished values per
     * CR 613.4d). This accessor is the legacy pre-switch fallback for direct {@code Permanent}
     * readers (views' raw term, last-known-information reads, predicate leaves).
     */
    public int getEffectivePower() {
        return getBasePower() + powerModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
    }

    /** The toughness counterpart of {@link #getEffectivePower()} — same pre-switch caveat. */
    public int getEffectiveToughness() {
        return getBaseToughness() + toughnessModifier + getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
    }

    /**
     * The base power of this permanent from its own stored state, WITHOUT modifiers or counters.
     * This is NOT the CR 613 layer-7b precedence decision — that lives in the layered pass
     * ({@code LayerSystemService}), which orders every base-P/T setter (static, one-shot,
     * animation, exchange) by timestamp and surfaces the winner through
     * {@code GameQueryService.getEffectivePower}. This accessor is the legacy fallback used by
     * direct {@code Permanent} readers (views, last-known-information reads, predicate leaves)
     * when no layered 7b entry applies; when only one of these fields is set, the two agree.
     */
    public int getBasePower() {
        if (basePowerToughnessOverriddenUntilEndOfTurn) {
            return basePowerOverride;
        }
        if (basePowerOverriddenPermanently) {
            return permanentBasePowerOverride;
        }
        if (animatedUntilEndOfTurn || animatedUntilEndOfCombat) {
            return animatedPower;
        }
        if (animatedUntilNextTurn) {
            return untilNextTurnAnimatedPower;
        }
        if (permanentlyAnimated) {
            return permanentAnimatedPower;
        }
        if (getCounterCount(CounterType.AWAKENING) > 0 && !card.hasType(CardType.CREATURE)) {
            return 8;
        }
        return card.getPower() != null ? card.getPower() : 0;
    }

    /** The base toughness counterpart of {@link #getBasePower()} — same fallback caveats. */
    public int getBaseToughness() {
        if (basePowerToughnessOverriddenUntilEndOfTurn) {
            return baseToughnessOverride;
        }
        if (baseToughnessOverriddenPermanently) {
            return permanentBaseToughnessOverride;
        }
        if (animatedUntilEndOfTurn || animatedUntilEndOfCombat) {
            return animatedToughness;
        }
        if (animatedUntilNextTurn) {
            return untilNextTurnAnimatedToughness;
        }
        if (permanentlyAnimated) {
            return permanentAnimatedToughness;
        }
        if (getCounterCount(CounterType.AWAKENING) > 0 && !card.hasType(CardType.CREATURE)) {
            return 8;
        }
        return card.getToughness() != null ? card.getToughness() : 0;
    }

    public CardColor getEffectiveColor() {
        if (colorOverridden && !transientColors.isEmpty()) {
            return transientColors.iterator().next();
        }
        if ((animatedUntilEndOfTurn || animatedUntilEndOfCombat) && animatedColor != null) {
            return animatedColor;
        }
        if (getCounterCount(CounterType.AWAKENING) > 0) {
            return CardColor.GREEN;
        }
        return card.getColor();
    }

    public boolean hasKeyword(Keyword keyword) {
        if (losesAllAbilitiesUntilEndOfTurn) return false;
        // Changeling grants all creature types; losing all creature types nullifies that grant.
        if (keyword == Keyword.CHANGELING && losesAllCreatureTypesUntilEndOfTurn) return false;
        if (removedKeywords.contains(keyword)) return false;
        return card.getKeywords().contains(keyword) || grantedKeywords.contains(keyword)
                || untilNextTurnKeywords.contains(keyword);
    }

    public void addTemporaryTriggeredEffect(EffectSlot slot, CardEffect effect) {
        temporaryTriggeredEffects.computeIfAbsent(slot, k -> new ArrayList<>()).add(effect);
    }

    public List<CardEffect> getTemporaryTriggeredEffects(EffectSlot slot) {
        return temporaryTriggeredEffects.getOrDefault(slot, List.of());
    }

    public void resetModifiers() {
        this.powerModifier = 0;
        this.toughnessModifier = 0;
        this.basePowerToughnessOverriddenUntilEndOfTurn = false;
        this.basePowerOverride = 0;
        this.baseToughnessOverride = 0;
        this.cantBeBlocked = false;
        this.cantBlockThisTurn = false;
        this.mustAttackThisTurn = false;
        this.mustAttackTargetId = null;
        this.mustBeBlockedThisTurn = false;
        this.mustBeBlockedByAllThisTurn = false;
        this.cantRegenerateThisTurn = false;
        this.exileInsteadOfDieThisTurn = false;
        this.hasDamageToOpponentCreatureBounce = false;
        this.temporaryTriggeredEffects.clear();
        this.animatedUntilEndOfTurn = false;
        this.animatedUntilEndOfCombat = false;
        this.animatedPower = 0;
        this.animatedToughness = 0;
        this.animatedColor = null;
        this.grantedKeywords.clear();
        this.removedKeywords.clear();
        this.transientColors.clear();
        this.colorOverridden = false;
        this.transientSubtypes.clear();
        this.transientLandTypeOverride = null;
        this.transientCreatureTypeOverride = null;
        this.grantedCardTypes.clear();
        this.protectionFromCardTypes.clear();
        this.protectionFromColorsUntilEndOfTurn.clear();
        this.protectionFromNonSubtypeCreaturesUntilEndOfTurn.clear();
        this.blockRestrictionsUntilEndOfTurn.clear();
        this.cantBlockIds.clear();
        this.mustBlockIds.clear();
        this.losesAllAbilitiesUntilEndOfTurn = false;
        this.losesAllCreatureTypesUntilEndOfTurn = false;
        this.temporaryActivatedAbilities.clear();
    }

    /**
     * Reverts an "until end of turn" copy (e.g. Tilonalli's Skinshifter) back to the permanent's
     * pre-copy card. Driven by the expiry of the copy's floating layer-1 effect at the cleanup
     * step (CR 613 layer engine), not by {@link #resetModifiers()}. Safe to call more than once
     * per turn — a second expired copy effect on the same permanent finds the flag cleared.
     */
    public void revertEndOfTurnCopy() {
        if (this.copyUntilEndOfTurn && this.preCopyCard != null) {
            this.card = this.preCopyCard;
        }
        this.copyUntilEndOfTurn = false;
        this.preCopyCard = null;
    }

    /**
     * Clears all "until your next turn" effects: activated abilities (e.g. Song of Freyalise)
     * and land animation (e.g. Sylvan Awakening). Called at the beginning of the controller's
     * next turn, not at end of turn.
     */
    public void clearUntilNextTurnEffects() {
        this.untilNextTurnActivatedAbilities.clear();
        this.animatedUntilNextTurn = false;
        this.untilNextTurnAnimatedPower = 0;
        this.untilNextTurnAnimatedToughness = 0;
        this.untilNextTurnSubtypes.clear();
        this.untilNextTurnKeywords.clear();
    }

    /**
     * Reverts an "until your next turn" copy (e.g. Shapesharer) back to the permanent's
     * pre-copy card. Called at the beginning of the ability controller's next turn.
     */
    public void revertUntilNextTurnCopy() {
        if (this.copyUntilControllerNextTurn && this.untilNextTurnPreCopyCard != null) {
            this.card = this.untilNextTurnPreCopyCard;
        }
        this.copyUntilControllerNextTurn = false;
        this.untilNextTurnPreCopyCard = null;
        this.copyUntilNextTurnControllerId = null;
    }
}
