package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
    @Setter private String manaCost;
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
     */
    public List<TargetFilter> getMultiTargetFilters() {
        return spellTargets.stream().map(SpellTarget::getFilter).toList();
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
        return effectRegistrations.getOrDefault(slot, List.of()).stream()
                .map(EffectRegistration::effect)
                .toList();
    }

    public List<EffectRegistration> getEffectRegistrations(EffectSlot slot) {
        return effectRegistrations.getOrDefault(slot, List.of());
    }

    public void addEffect(EffectSlot slot, CardEffect effect) {
        effectRegistrations.computeIfAbsent(slot, k -> new ArrayList<>()).add(new EffectRegistration(effect));
    }

    public void addEffect(EffectSlot slot, CardEffect effect, TriggerMode triggerMode) {
        effectRegistrations.computeIfAbsent(slot, k -> new ArrayList<>()).add(new EffectRegistration(effect, triggerMode));
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

    public void addActivatedAbility(ActivatedAbility ability) {
        activatedAbilities.add(ability);
    }

    public void addGraveyardActivatedAbility(ActivatedAbility ability) {
        graveyardActivatedAbilities.add(ability);
    }

    public String getBackFaceClassName() {
        return null;
    }

    public int getManaValue() {
        if (manaCost == null) return 0;
        return new ManaCost(manaCost).getManaValue();
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

    public Set<TargetType> getAllowedTargets() {
        Set<TargetType> result = EnumSet.noneOf(TargetType.class);
        if (isAura()) {
            if (isEnchantPlayer()) {
                result.add(TargetType.PLAYER);
            } else {
                result.add(TargetType.PERMANENT);
            }
        }
        for (CardEffect e : getEffects(EffectSlot.SPELL)) {
            collectTargetTypes(e, result);
        }
        for (CardEffect e : getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (e.canTargetPlayer()) result.add(TargetType.PLAYER);
            if (e.canTargetPermanent()) result.add(TargetType.PERMANENT);
        }
        return result;
    }

    private void collectTargetTypes(CardEffect e, Set<TargetType> out) {
        if (e.canTargetPlayer()) out.add(TargetType.PLAYER);
        if (e.canTargetPermanent()) out.add(TargetType.PERMANENT);
        if (e.canTargetSpell()) out.add(TargetType.SPELL_ON_STACK);
        if (e.canTargetGraveyard()) out.add(TargetType.GRAVEYARD);
        if (e.canTargetExile()) out.add(TargetType.EXILE);
    }

    public boolean isNeedsTarget() {
        Set<TargetType> t = getAllowedTargets();
        return t.contains(TargetType.PLAYER) || t.contains(TargetType.PERMANENT)
                || t.contains(TargetType.GRAVEYARD) || t.contains(TargetType.EXILE);
    }

    /**
     * Returns true if the spell itself requires a target to be cast (MTG rule 601.2c).
     * Unlike {@link #isNeedsTarget()}, this excludes ON_ENTER_BATTLEFIELD effects because
     * ETB abilities are separate from casting — a creature is always castable regardless of
     * whether its ETB can find a valid target. Also excludes {@link CostEffect}s because
     * sacrifice/discard costs are not "targeting" in MTG terms.
     */
    public boolean isNeedsSpellCastTarget() {
        Set<TargetType> result = EnumSet.noneOf(TargetType.class);
        if (isAura()) {
            if (isEnchantPlayer()) {
                result.add(TargetType.PLAYER);
            } else {
                result.add(TargetType.PERMANENT);
            }
        }
        for (CardEffect e : getEffects(EffectSlot.SPELL)) {
            if (e instanceof CostEffect) continue;
            collectTargetTypes(e, result);
        }
        return result.contains(TargetType.PLAYER) || result.contains(TargetType.PERMANENT)
                || result.contains(TargetType.GRAVEYARD) || result.contains(TargetType.EXILE);
    }

    public boolean isNeedsSpellTarget() {
        return getAllowedTargets().contains(TargetType.SPELL_ON_STACK);
    }

    public boolean isNeedsDamageDistribution() {
        boolean inSpell = getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect
                        || e instanceof DealDividedDamageAmongTargetCreaturesEffect);
        boolean inAbility = activatedAbilities.stream()
                .flatMap(a -> a.getEffects().stream())
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect
                        || e instanceof DealDividedDamageAmongTargetCreaturesEffect);
        return inSpell || inAbility;
    }
}
