package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
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

    public static void clearOracleRegistry() {
        oracleRegistry.clear();
    }

    private final UUID id = UUID.randomUUID();
    @Setter private String name;
    @Setter private CardType type;
    private String manaCost;
    /** Cached parsed ManaCost, invalidated on setManaCost. */
    @Getter(AccessLevel.NONE)
    private ManaCost parsedManaCost;
    @Setter private CardColor color;
    @Setter private List<CardColor> colors = List.of();

    @Setter private Set<CardType> additionalTypes = Set.of();
    @Setter private Set<CardSupertype> supertypes = Set.of();
    @Setter private List<CardSubtype> subtypes = List.of();
    @Setter private String cardText;
    @Setter private Integer power;
    @Setter private Integer toughness;
    @Setter private Set<Keyword> keywords = Set.of();
    @Setter private Integer loyalty;
    @Setter private ManaColor xColorRestriction;
    @Setter private String setCode;
    @Setter private String collectorNumber;

    @Setter private boolean token;
    @Setter private boolean requiresCreatureMana;
    @Setter private int additionalCostPerExtraTarget;
    /**
     * When true, the same permanent may be chosen for different target groups (CR 114.6c).
     * By default, targets across groups must be distinct — matching the common MTG pattern
     * where separate "target" instances imply "another". Set this for cards whose oracle text
     * does NOT use "another" and whose target filters can overlap (e.g. "target creature" +
     * "target Merfolk", where a Merfolk satisfies both).
     */
    @Setter private boolean allowSharedTargets;

    // Target-first targeting system: each target() call adds a SpellTarget
    @Getter(AccessLevel.NONE)
    private final List<SpellTarget> spellTargets = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private final Map<CardEffect, Integer> effectTargetIndexMap = new IdentityHashMap<>();
    // Runtime override set by modal spells (ChooseOneEffect) at cast time
    @Setter private TargetFilter castTimeTargetFilter;
    @Setter private Card imprintedCard;
    @Setter private String watermark;
    @Setter private Card backFaceCard;
    private List<CastingOption> castingOptions = new ArrayList<>();

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

    public Card() {
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
        SpellTarget st = new SpellTarget(this, filter, minTargets, maxTargets, spellTargets.size());
        spellTargets.add(st);
        return st;
    }

    /**
     * Called by {@link SpellTarget#addEffect} to map an effect instance to its target index.
     */
    public void registerEffectTargetIndex(CardEffect effect, int targetIndex) {
        effectTargetIndexMap.put(effect, targetIndex);
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
                    if (entry.getValue() == groupIndex && entry.getKey().canTargetPlayer()) {
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
        for (SpellTarget st : original.spellTargets) {
            spellTargets.add(new SpellTarget(this, st.getFilter(), st.getMinTargets(), st.getMaxTargets(), st.getIndex()));
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
        if (keywords.contains(keyword)) {
            var mutable = EnumSet.copyOf(keywords);
            mutable.remove(keyword);
            this.keywords = mutable;
        }
    }

    public void addEffect(EffectSlot slot, CardEffect effect) {
        validateEffectSlotType(slot, effect);
        effectRegistrations.computeIfAbsent(slot, k -> new ArrayList<>()).add(new EffectRegistration(effect));
        effectCache.remove(slot);
    }

    public void addEffect(EffectSlot slot, CardEffect effect, TriggerMode triggerMode) {
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
        castingOptions.add(option);
    }

    public <T extends CastingOption> Optional<T> getCastingOption(Class<T> type) {
        return castingOptions.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    public void setSagaChapterTargetFilter(EffectSlot slot, Set<TargetFilter> filters) {
        sagaChapterTargetFilters.put(slot, filters);
    }

    public Set<TargetFilter> getSagaChapterTargetFilters(EffectSlot slot) {
        return sagaChapterTargetFilters.getOrDefault(slot, Set.of());
    }

    public void addActivatedAbility(ActivatedAbility ability) {
        activatedAbilities.add(ability);
    }

    public void addGraveyardActivatedAbility(ActivatedAbility ability) {
        graveyardActivatedAbilities.add(ability);
    }

    public String getBackFaceClassName() {
        return null;
    }

    public void setManaCost(String manaCost) {
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
        return isAura() && subtypes.contains(CardSubtype.CURSE);
    }

}
