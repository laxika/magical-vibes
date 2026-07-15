package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Card {

    private static final Map<String, OracleData> oracleRegistry = new ConcurrentHashMap<>();

    public static void registerOracle(String className, OracleData data) {
        oracleRegistry.put(className, data);
    }

    /**
     * Registers oracle data only if the class has none yet. Used for back-face registrations: a
     * back face may name a standalone card class (prepare-spell cards reuse the real spell's
     * class), and the face-level data must not clobber the richer data registered from that
     * card's own printing — Scryfall face nodes can lack fields such as colors.
     */
    public static void registerOracleIfAbsent(String className, OracleData data) {
        oracleRegistry.putIfAbsent(className, data);
    }

    public static void clearOracleRegistry() {
        oracleRegistry.clear();
    }

    private final UUID id;
    /**
     * When true, this card is live in a game (part of a deck, on the stack, or wrapped in a
     * {@link Permanent}) and must no longer be mutated: live Card instances are shared with AI
     * simulation copies ({@code GameData.simulationCopy()}), so any mutation would leak between
     * the real game and simulated games. Runtime state belongs on the {@code Permanent}, the
     * {@code StackEntry}, or {@code GameData} — or mutate a {@link #createRuntimeCopy()} instead.
     */
    @Getter(AccessLevel.NONE)
    private boolean frozen;
    /**
     * The player who owns this card — the player whose deck it started the game in. Stamped once
     * at game setup ({@code GameSetupService}) and preserved across zone changes. Cards created by
     * the engine (tokens, copies) leave this {@code null}. Distinct from control: used to evaluate
     * "a spell you don't own" (e.g. Nita, Forum Conciliator).
     */
    private UUID ownerId;
    private String name;
    private CardType type;
    private String manaCost;
    /** Cached parsed ManaCost, invalidated on setManaCost. */
    @Getter(AccessLevel.NONE)
    private ManaCost parsedManaCost;
    private CardColor color;
    private List<CardColor> colors = List.of();

    private Set<CardType> additionalTypes = Set.of();
    private Set<CardSupertype> supertypes = Set.of();
    private List<CardSubtype> subtypes = List.of();
    private String cardText;
    private Integer power;
    private Integer toughness;
    private Set<Keyword> keywords = Set.of();
    private Integer loyalty;
    private ManaColor xColorRestriction;
    private String setCode;
    private String collectorNumber;

    private boolean token;
    /** "This spell can't be copied." Honored by the copy effect handlers. */
    private boolean cantBeCopied;
    /**
     * When true, the permanent this card becomes is registered for sacrifice at the beginning of
     * the next end step (e.g. the token created by copying a creature spell with Choreographed Sparks).
     */
    private boolean sacrificeAtEndStep;
    private boolean requiresCreatureMana;
    /**
     * When true, this Aura enchants a player even though it isn't a Curse (e.g. Wheel of Sun and
     * Moon). Curses are recognized automatically; non-Curse "Enchant player" auras must set this.
     */
    private boolean enchantPlayer;
    private int additionalCostPerExtraTarget;
    /**
     * When true, the same permanent may be chosen for different target groups (CR 114.6c).
     * By default, targets across groups must be distinct — matching the common MTG pattern
     * where separate "target" instances imply "another". Set this for cards whose oracle text
     * does NOT use "another" and whose target filters can overlap (e.g. "target creature" +
     * "target Merfolk", where a Merfolk satisfies both).
     */
    private boolean allowSharedTargets;
    /**
     * Optional cross-target restriction on the whole set of chosen targets (e.g. Rivals' Duel's
     * "two target creatures that share no creature types"), checked at announcement in addition
     * to the per-position filters. Null for spells with no such restriction.
     */
    private MultiTargetConstraint multiTargetConstraint;

    // Target-first targeting system: each target() call adds a SpellTarget
    @Getter(AccessLevel.NONE)
    private final List<SpellTarget> spellTargets = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private final Map<CardEffect, Integer> effectTargetIndexMap = new IdentityHashMap<>();
    // Runtime override set by modal spells (ChooseOneEffect) at cast time — only ever written on
    // an unfrozen runtime copy (see SpellCastingService's modal copy-on-cast)
    private TargetFilter castTimeTargetFilter;
    private String watermark;
    private Card backFaceCard;
    private List<CastingOption> castingOptions = new ArrayList<>();
    /** Card-specific "cast this spell only when …" restriction, or null for normal timing. Defiant Stand. */
    private SpellCastTimingRestriction spellCastTimingRestriction;
    /**
     * Card-specific "cast this spell only if …" condition, evaluated for the caster when computing
     * playability, or null for no such restriction. Talara's Battalion ("only if you've cast another
     * green spell this turn").
     */
    private Condition castCondition;

    @Getter(AccessLevel.NONE)
    private Map<EffectSlot, List<EffectRegistration>> effectRegistrations = new EnumMap<>(EffectSlot.class);
    /** Cached effect-only lists, invalidated on addEffect. */
    @Getter(AccessLevel.NONE)
    private Map<EffectSlot, List<CardEffect>> effectCache = new EnumMap<>(EffectSlot.class);
    /** Per-chapter target filters for Saga cards (e.g. "target creature an opponent controls"). */
    @Getter(AccessLevel.NONE)
    private Map<EffectSlot, Set<TargetFilter>> sagaChapterTargetFilters = new EnumMap<>(EffectSlot.class);
    private List<ActivatedAbility> activatedAbilities = new ArrayList<>();
    private List<ActivatedAbility> graveyardActivatedAbilities = new ArrayList<>();
    /** Abilities activatable while this card is in its owner's hand (e.g. Reinforce). */
    private List<ActivatedAbility> handActivatedAbilities = new ArrayList<>();

    public Card() {
        this.id = UUID.randomUUID();
        OracleData oracle = oracleRegistry.get(getClass().getSimpleName());
        if (oracle != null) {
            this.name = oracle.name();
            this.type = oracle.type();
            this.additionalTypes = oracle.additionalTypes();
            this.manaCost = oracle.manaCost();
            this.color = oracle.color();
            this.colors = oracle.colors();
            this.supertypes = oracle.supertypes();
            this.subtypes = oracle.subtypes();
            this.cardText = oracle.cardText();
            this.power = oracle.power();
            this.toughness = oracle.toughness();
            this.keywords = oracle.keywords();
            this.loyalty = oracle.loyalty();
            this.watermark = oracle.watermark();
        }
    }

    /**
     * Copy constructor backing {@link #createRuntimeCopy()}. Copies every field, including the
     * id, but not {@link #frozen} — the copy starts mutable. Collection fields are copied into
     * fresh containers (elements shared: effects and abilities are immutable). The copied
     * {@link SpellTarget}s keep their back-reference to the source card; that reference is only
     * used by the construction-time builder API, never at runtime. Note the copy is a plain
     * {@code Card} — subclass identity (only used for oracle-registry lookup at construction
     * and {@code getBackFaceClassName()} at set load) is not preserved.
     *
     * <p>MAINTENANCE: when adding a field to Card, copy it here. {@code CardRuntimeCopyTest}
     * fails on any newly declared field to force this update.
     */
    protected Card(Card source) {
        this.id = source.id;
        this.ownerId = source.ownerId;
        this.name = source.name;
        this.type = source.type;
        this.manaCost = source.manaCost;
        this.parsedManaCost = source.parsedManaCost;
        this.color = source.color;
        this.colors = source.colors;
        this.additionalTypes = source.additionalTypes;
        this.supertypes = source.supertypes;
        this.subtypes = source.subtypes;
        this.cardText = source.cardText;
        this.power = source.power;
        this.toughness = source.toughness;
        this.keywords = source.keywords;
        this.loyalty = source.loyalty;
        this.xColorRestriction = source.xColorRestriction;
        this.setCode = source.setCode;
        this.collectorNumber = source.collectorNumber;
        this.token = source.token;
        this.cantBeCopied = source.cantBeCopied;
        this.sacrificeAtEndStep = source.sacrificeAtEndStep;
        this.requiresCreatureMana = source.requiresCreatureMana;
        this.enchantPlayer = source.enchantPlayer;
        this.additionalCostPerExtraTarget = source.additionalCostPerExtraTarget;
        this.allowSharedTargets = source.allowSharedTargets;
        this.multiTargetConstraint = source.multiTargetConstraint;
        this.spellTargets.addAll(source.spellTargets);
        this.effectTargetIndexMap.putAll(source.effectTargetIndexMap);
        this.castTimeTargetFilter = source.castTimeTargetFilter;
        this.watermark = source.watermark;
        this.backFaceCard = source.backFaceCard;
        this.castingOptions = new ArrayList<>(source.castingOptions);
        this.spellCastTimingRestriction = source.spellCastTimingRestriction;
        this.castCondition = source.castCondition;
        source.effectRegistrations.forEach((slot, regs) ->
                this.effectRegistrations.put(slot, new ArrayList<>(regs)));
        // effectCache intentionally left empty — rebuilt lazily by getEffects()
        this.sagaChapterTargetFilters.putAll(source.sagaChapterTargetFilters);
        this.activatedAbilities = new ArrayList<>(source.activatedAbilities);
        this.graveyardActivatedAbilities = new ArrayList<>(source.graveyardActivatedAbilities);
        this.handActivatedAbilities = new ArrayList<>(source.handActivatedAbilities);
    }

    /**
     * Creates an unfrozen copy of this card with the same id, for flows that must write
     * cast-time state onto a card (modal spells choosing a mode, AI mode evaluation). The
     * copy replaces the original in the zone it is cast from and travels on from there;
     * the shared original is never mutated.
     */
    public Card createRuntimeCopy() {
        return new Card(this);
    }

    // ── Freeze guard ─────────────────────────────────────────────────

    /**
     * Marks this card as live: from now on every mutator throws. Called when the card joins
     * live game structures (deck stamping in {@code GameSetupService}, {@code Permanent} and
     * {@code StackEntry} construction). Idempotent.
     */
    public void freeze() {
        this.frozen = true;
    }

    private void assertMutable() {
        if (frozen) {
            throw new IllegalStateException("Card '" + name + "' (" + id + ") is frozen. Live cards are shared"
                    + " with AI simulation copies and must not be mutated — store runtime state on the Permanent,"
                    + " the StackEntry, or GameData, or mutate a createRuntimeCopy() instead.");
        }
    }

    // ── Guarded setters (hand-written instead of @Setter so assertMutable() runs) ──

    public void setOwnerId(UUID ownerId) { assertMutable(); this.ownerId = ownerId; }
    public void setName(String name) { assertMutable(); this.name = name; }
    public void setType(CardType type) { assertMutable(); this.type = type; }
    public void setColor(CardColor color) { assertMutable(); this.color = color; }
    public void setColors(List<CardColor> colors) { assertMutable(); this.colors = colors; }
    public void setAdditionalTypes(Set<CardType> additionalTypes) { assertMutable(); this.additionalTypes = additionalTypes; }
    public void setSupertypes(Set<CardSupertype> supertypes) { assertMutable(); this.supertypes = supertypes; }
    public void setSubtypes(List<CardSubtype> subtypes) { assertMutable(); this.subtypes = subtypes; }
    public void setCardText(String cardText) { assertMutable(); this.cardText = cardText; }
    public void setPower(Integer power) { assertMutable(); this.power = power; }
    public void setToughness(Integer toughness) { assertMutable(); this.toughness = toughness; }
    public void setKeywords(Set<Keyword> keywords) { assertMutable(); this.keywords = keywords; }
    public void setLoyalty(Integer loyalty) { assertMutable(); this.loyalty = loyalty; }
    public void setXColorRestriction(ManaColor xColorRestriction) { assertMutable(); this.xColorRestriction = xColorRestriction; }
    public void setSetCode(String setCode) { assertMutable(); this.setCode = setCode; }
    public void setCollectorNumber(String collectorNumber) { assertMutable(); this.collectorNumber = collectorNumber; }
    public void setToken(boolean token) { assertMutable(); this.token = token; }
    public void setCantBeCopied(boolean cantBeCopied) { assertMutable(); this.cantBeCopied = cantBeCopied; }
    public void setSacrificeAtEndStep(boolean sacrificeAtEndStep) { assertMutable(); this.sacrificeAtEndStep = sacrificeAtEndStep; }
    public void setRequiresCreatureMana(boolean requiresCreatureMana) { assertMutable(); this.requiresCreatureMana = requiresCreatureMana; }
    public void setEnchantPlayer(boolean enchantPlayer) { assertMutable(); this.enchantPlayer = enchantPlayer; }
    public void setAdditionalCostPerExtraTarget(int additionalCostPerExtraTarget) { assertMutable(); this.additionalCostPerExtraTarget = additionalCostPerExtraTarget; }
    public void setAllowSharedTargets(boolean allowSharedTargets) { assertMutable(); this.allowSharedTargets = allowSharedTargets; }
    public void setMultiTargetConstraint(MultiTargetConstraint multiTargetConstraint) { assertMutable(); this.multiTargetConstraint = multiTargetConstraint; }
    public void setCastTimeTargetFilter(TargetFilter castTimeTargetFilter) { assertMutable(); this.castTimeTargetFilter = castTimeTargetFilter; }
    public void setSpellCastTimingRestriction(SpellCastTimingRestriction spellCastTimingRestriction) { assertMutable(); this.spellCastTimingRestriction = spellCastTimingRestriction; }
    public void setCastCondition(Condition castCondition) { assertMutable(); this.castCondition = castCondition; }
    public void setWatermark(String watermark) { assertMutable(); this.watermark = watermark; }
    public void setBackFaceCard(Card backFaceCard) { assertMutable(); this.backFaceCard = backFaceCard; }

    // ── Target-first builder API ──────────────────────────────────────

    /**
     * Declares a required target (min=1, max=1) and returns a builder
     * whose {@code addEffect()} associates effects with this target.
     */
    public SpellTarget target(TargetFilter filter) {
        return target(filter, 1, 1);
    }

    /**
     * Declares an unfiltered target with custom min/max counts.
     */
    public SpellTarget target(int minTargets, int maxTargets) {
        return target(null, minTargets, maxTargets);
    }

    /**
     * Declares a target with custom min/max counts and returns a builder
     * whose {@code addEffect()} associates effects with this target.
     */
    public SpellTarget target(TargetFilter filter, int minTargets, int maxTargets) {
        assertMutable();
        SpellTarget st = new SpellTarget(this, filter, minTargets, maxTargets, spellTargets.size());
        spellTargets.add(st);
        return st;
    }

    /**
     * Declares a target group whose target count scales with the spell's X value
     * ("Destroy X target nonblack creatures" — Dregs of Sorrow). The effective number of
     * targets is bounded by X at cast time; {@code cap} is only a sanity ceiling.
     */
    public SpellTarget targetX(TargetFilter filter, int cap) {
        assertMutable();
        SpellTarget st = new SpellTarget(this, filter, 0, cap, spellTargets.size(), true);
        spellTargets.add(st);
        return st;
    }

    /**
     * Called by {@link SpellTarget#addEffect} to map an effect instance to its target index.
     * Wrapper effects (conditional, may) register their inner effects under the same index,
     * because resolution unwraps them before dispatching to the handler — the handler must be
     * able to look up the group by the effect instance it actually receives.
     */
    public void registerEffectTargetIndex(CardEffect effect, int targetIndex) {
        assertMutable();
        effectTargetIndexMap.put(effect, targetIndex);
        switch (effect) {
            case ConditionalEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            case ConditionalReplacementEffect e -> {
                if (e.baseEffect() != null) registerEffectTargetIndex(e.baseEffect(), targetIndex);
                registerEffectTargetIndex(e.upgradedEffect(), targetIndex);
            }
            case MayEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            case MayPayManaEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            case MayPayTapPermanentsEffect e -> registerEffectTargetIndex(e.wrapped(), targetIndex);
            default -> { }
        }
    }

    /**
     * Clears runtime target-first declarations. Used by modal spells (ChooseOneEffect) whose chosen
     * mode declares its own {@code target()} slots at cast time, so re-casting the same card instance
     * does not accumulate stale target declarations.
     */
    public void clearRuntimeSpellTargets() {
        assertMutable();
        spellTargets.clear();
        effectTargetIndexMap.clear();
    }

    // ── Derived targeting getters (replace old stored fields) ────────

    /**
     * Returns the target filter for single-target spells, or the first target's
     * filter for multi-target spells. For modal spells, returns the cast-time override.
     */
    public TargetFilter getTargetFilter() {
        if (castTimeTargetFilter != null) return castTimeTargetFilter;
        if (spellTargets.isEmpty()) return null;
        return spellTargets.getFirst().getFilter();
    }

    /**
     * Returns per-position target filters for multi-target spells.
     * Each target group's filter is repeated for its maxTargets count,
     * so that position-based validation matches the correct filter
     * when a group allows multiple targets (e.g. "up to 2 target creatures").
     */
    public List<TargetFilter> getMultiTargetFilters() {
        List<TargetFilter> expanded = new ArrayList<>();
        for (SpellTarget st : spellTargets) {
            for (int i = 0; i < st.getMaxTargets(); i++) {
                expanded.add(st.getFilter());
            }
        }
        return expanded;
    }

    /**
     * Returns the minimum total number of targets required.
     */
    public int getMinTargets() {
        return spellTargets.stream().mapToInt(SpellTarget::getMinTargets).sum();
    }

    /**
     * Returns the maximum total number of targets allowed.
     */
    public int getMaxTargets() {
        return spellTargets.stream().mapToInt(SpellTarget::getMaxTargets).sum();
    }

    /**
     * Returns true if any target group scales its target count with the spell's X value.
     */
    public boolean hasXScaledTargets() {
        return spellTargets.stream().anyMatch(SpellTarget::isXScaled);
    }

    /**
     * Returns the minimum total number of targets required for the given X value.
     * X-scaled groups contribute {@code min(xValue, minTargets)}; others contribute their static minimum.
     */
    public int getEffectiveMinTargets(int xValue) {
        return spellTargets.stream()
                .mapToInt(st -> st.isXScaled() ? Math.min(xValue, st.getMinTargets()) : st.getMinTargets())
                .sum();
    }

    /**
     * Returns the maximum total number of targets allowed for the given X value.
     * X-scaled groups contribute {@code min(xValue, maxTargets)}; others contribute their static maximum.
     */
    public int getEffectiveMaxTargets(int xValue) {
        return spellTargets.stream()
                .mapToInt(st -> st.isXScaled() ? Math.min(xValue, st.getMaxTargets()) : st.getMaxTargets())
                .sum();
    }

    /**
     * Returns the spell target declarations.
     */
    public List<SpellTarget> getSpellTargets() {
        return spellTargets;
    }

    /**
     * Returns the target index for the given effect instance, or -1 if not mapped.
     */
    public int getEffectTargetIndex(CardEffect effect) {
        return effectTargetIndexMap.getOrDefault(effect, -1);
    }

    /**
     * True if any spell effect is bound (via {@code target(...).addEffect(...)}) to the given target
     * group. A group with no bound effect is a bare positional target that another effect reads by
     * index (e.g. Blood Feud's first fight target, read by the {@code FightTargetsEffect} bound to the
     * second group); such a group is never a gated-out trigger group.
     */
    public boolean bindsEffectToTargetGroup(int groupIndex) {
        return effectTargetIndexMap.containsValue(groupIndex);
    }

    /**
     * Returns true if the target group at the given expanded position allows player targets.
     * Used by the valid target service to determine per-position player targeting in multi-target spells.
     */
    public boolean doesPositionAllowPlayerTargets(int expandedPosition) {
        if (spellTargets.isEmpty()) return false;
        int cumulative = 0;
        for (SpellTarget st : spellTargets) {
            cumulative += st.getMaxTargets();
            if (expandedPosition < cumulative) {
                int groupIndex = st.getIndex();
                for (Map.Entry<CardEffect, Integer> entry : effectTargetIndexMap.entrySet()) {
                    if (entry.getValue() == groupIndex && entry.getKey().targetSpec().category().includesPlayers()) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Copies targeting configuration from another card (used by spell copy effects).
     */
    public void copyTargetingFrom(Card original) {
        assertMutable();
        for (SpellTarget st : original.spellTargets) {
            spellTargets.add(new SpellTarget(this, st.getFilter(), st.getMinTargets(), st.getMaxTargets(), st.getIndex(), st.isXScaled()));
        }
        effectTargetIndexMap.putAll(original.effectTargetIndexMap);
        castTimeTargetFilter = original.castTimeTargetFilter;
    }

    // ── Effect management ───────────────────────────────────────────

    public List<CardEffect> getEffects(EffectSlot slot) {
        List<CardEffect> cached = effectCache.get(slot);
        if (cached != null) return cached;
        List<EffectRegistration> regs = effectRegistrations.get(slot);
        if (regs == null) return List.of();
        List<CardEffect> effects = new ArrayList<>(regs.size());
        for (EffectRegistration reg : regs) {
            effects.add(reg.effect());
        }
        List<CardEffect> unmodifiable = Collections.unmodifiableList(effects);
        effectCache.put(slot, unmodifiable);
        return unmodifiable;
    }

    public List<EffectRegistration> getEffectRegistrations(EffectSlot slot) {
        return effectRegistrations.getOrDefault(slot, List.of());
    }

    public void removeKeyword(Keyword keyword) {
        assertMutable();
        if (keywords.contains(keyword)) {
            var mutable = EnumSet.copyOf(keywords);
            mutable.remove(keyword);
            this.keywords = mutable;
        }
    }

    public void addEffect(EffectSlot slot, CardEffect effect) {
        assertMutable();
        validateEffectSlotType(slot, effect);
        effectRegistrations.computeIfAbsent(slot, k -> new ArrayList<>()).add(new EffectRegistration(effect));
        effectCache.remove(slot);
    }

    public void addEffect(EffectSlot slot, CardEffect effect, TriggerMode triggerMode) {
        assertMutable();
        validateEffectSlotType(slot, effect);
        effectRegistrations.computeIfAbsent(slot, k -> new ArrayList<>()).add(new EffectRegistration(effect, triggerMode));
        effectCache.remove(slot);
    }

    private void validateEffectSlotType(EffectSlot slot, CardEffect effect) {
        if (slot == EffectSlot.STATE_TRIGGERED && !(effect instanceof StateTriggerEffect)) {
            throw new IllegalArgumentException(
                    "STATE_TRIGGERED slot requires StateTriggerEffect, got " + effect.getClass().getSimpleName());
        }
    }

    public void addCastingOption(CastingOption option) {
        assertMutable();
        castingOptions.add(option);
    }

    public <T extends CastingOption> Optional<T> getCastingOption(Class<T> type) {
        return castingOptions.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    public void setSagaChapterTargetFilter(EffectSlot slot, Set<TargetFilter> filters) {
        assertMutable();
        sagaChapterTargetFilters.put(slot, filters);
    }

    public Set<TargetFilter> getSagaChapterTargetFilters(EffectSlot slot) {
        return sagaChapterTargetFilters.getOrDefault(slot, Set.of());
    }

    public void addActivatedAbility(ActivatedAbility ability) {
        assertMutable();
        activatedAbilities.add(ability);
    }

    public void addGraveyardActivatedAbility(ActivatedAbility ability) {
        assertMutable();
        graveyardActivatedAbilities.add(ability);
    }

    public void addHandActivatedAbility(ActivatedAbility ability) {
        assertMutable();
        handActivatedAbilities.add(ability);
    }

    public String getBackFaceClassName() {
        return null;
    }

    public void setManaCost(String manaCost) {
        assertMutable();
        this.manaCost = manaCost;
        this.parsedManaCost = null;
    }

    /**
     * Returns a cached parsed {@link ManaCost} for this card's mana cost string.
     * Returns {@code null} if this card has no mana cost.
     */
    public ManaCost getParsedManaCost() {
        if (manaCost == null) return null;
        ManaCost cached = parsedManaCost;
        if (cached == null) {
            cached = new ManaCost(manaCost);
            parsedManaCost = cached;
        }
        return cached;
    }

    public int getManaValue() {
        ManaCost cost = getParsedManaCost();
        return cost != null ? cost.getManaValue() : 0;
    }

    public boolean hasType(CardType cardType) {
        return type == cardType || additionalTypes.contains(cardType);
    }

    public boolean isAura() {
        return subtypes.contains(CardSubtype.AURA);
    }

    public boolean isSaga() {
        return subtypes.contains(CardSubtype.SAGA);
    }

    /**
     * Returns the final chapter number for a Saga card (e.g. 3 for a three-chapter Saga).
     * Returns 0 if the card has no chapter abilities.
     */
    public int getSagaFinalChapter() {
        if (!getEffects(EffectSlot.SAGA_CHAPTER_III).isEmpty()) return 3;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_II).isEmpty()) return 2;
        if (!getEffects(EffectSlot.SAGA_CHAPTER_I).isEmpty()) return 1;
        return 0;
    }

    public boolean isEnchantPlayer() {
        return isAura() && (subtypes.contains(CardSubtype.CURSE) || enchantPlayer);
    }

}
