package com.github.laxika.magicalvibes.model.layer;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The mutable working copy of one permanent's characteristics used during the CR 613 layered
 * pass (see {@code agent-docs/LAYER_SYSTEM.md}). One instance per permanent is seeded from the
 * post-copy card (layer 1 already applied to the card identity) plus the permanent's persistent
 * one-shot grants and counters, then mutated layer by layer (L2..L7d) across the whole
 * battlefield. Predicates evaluated while applying layer N read these states as of the layers
 * already applied.
 *
 * <p>Overrides ("becomes a Treefolk", "becomes blue") simply clear the relevant collection
 * before adding — there is no separate "was overridden" flag.
 */
@Getter
public class CharacteristicState {

    private String name;
    private final Set<CardType> cardTypes = EnumSet.noneOf(CardType.class);
    private final Set<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
    /** Ordered, as printed subtypes are (CR 205.3). */
    private final List<CardSubtype> subtypes = new ArrayList<>();
    /** Always a set: the layer engine works on color sets even though {@link Card} holds one color. */
    private final Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
    /** True once a layer-5 color-setting effect ("becomes red", CR 105.3) replaced the colors. */
    private boolean colorsOverridden;
    /** The colors the state was seeded with (natural + persistent grants + legacy animation),
     *  snapshotted by {@link #snapshotSeededCharacteristics()} before layer 5 runs so the query
     *  layer can report additive color grants as the difference from this baseline. */
    private final Set<CardColor> seededColors = EnumSet.noneOf(CardColor.class);
    private final Set<Keyword> keywords = new HashSet<>();
    /** The keywords the state was seeded with (printed + persistent one-shot grants),
     *  snapshotted by {@link #snapshotSeededCharacteristics()} before layer 6 runs. */
    private final Set<Keyword> seededKeywords = new HashSet<>();
    /** Protection-from-color abilities (own printed protection plus layer-6 grants). */
    private final Set<CardColor> protectionColors = EnumSet.noneOf(CardColor.class);
    private final List<ActivatedAbility> grantedActivatedAbilities = new ArrayList<>();
    private final List<CardEffect> grantedStaticEffects = new ArrayList<>();
    @Setter private int basePower;
    @Setter private int baseToughness;
    /** Additive layer-7c contribution (boosts and +1/+1 / -1/-1 counters). */
    @Setter private int powerDelta;
    @Setter private int toughnessDelta;
    /** Number of layer-7d switches applied; an odd count means P/T are swapped. */
    private int switchCount;
    /** True once a "loses all abilities" effect has been applied in layer 6. */
    private boolean losesAllAbilities;
    /** CR 613.7 timestamp of the lose-all effect; only meaningful while {@link #losesAllAbilities}. */
    private long losesAllAbilitiesTimestamp;
    /**
     * True once a land-type-setting effect (Blood Moon, Sea's Claim, ...) removed the object's
     * printed abilities as part of setting its land types (CR 305.7). Unlike
     * {@link #losesAllAbilities} this happens in layer 4, so the object's own static-slot
     * effects stop existing for every later layer (and for later layer-4 applications —
     * CR 613.8 dependency ordering); externally granted abilities are unaffected.
     */
    private boolean printedAbilitiesRemoved;

    /**
     * Seeds the state from the permanent's post-copy card plus the persistent one-shot grants
     * and counters stored on the {@link Permanent}. Transient (until-end-of-turn etc.) state is
     * NOT seeded — in the target model those are floating continuous effects applied during the
     * pass.
     */
    public CharacteristicState(Card card, Permanent permanent) {
        this.name = card.getName();
        // Null-tolerant like the color/P/T seeding below: bare test cards carry only a name.
        if (card.getType() != null) {
            this.cardTypes.add(card.getType());
        }
        this.cardTypes.addAll(card.getAdditionalTypes());
        this.cardTypes.addAll(permanent.getPersistentGrantedCardTypes());
        this.supertypes.addAll(card.getSupertypes());
        this.subtypes.addAll(card.getSubtypes());
        for (CardSubtype granted : permanent.getGrantedSubtypes()) {
            addSubtype(granted);
        }
        // Seed every intrinsic color: a multicolored card ({W/U} hybrid) is all of its colors,
        // not just the first. Legacy animation/awakening/override colors replace this baseline in
        // LayerSystemService.seedLegacyColorAndAbilityState.
        if (card.getColors() != null && !card.getColors().isEmpty()) {
            this.colors.addAll(card.getColors());
        } else if (card.getColor() != null) {
            this.colors.add(card.getColor());
        }
        this.colors.addAll(permanent.getGrantedColors());
        this.keywords.addAll(card.getKeywords());
        this.grantedActivatedAbilities.addAll(permanent.getPersistentGrantedActivatedAbilities());
        this.basePower = card.getPower() != null ? card.getPower() : 0;
        this.baseToughness = card.getToughness() != null ? card.getToughness() : 0;
        int counterDelta = permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
                - permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
        this.powerDelta = counterDelta;
        this.toughnessDelta = counterDelta;
    }

    /**
     * Copy constructor for CR 613.8 dependency trials: the layer engine copies the in-progress
     * states, trial-applies effects onto the copies, and discards them. Copies every field
     * directly (no mutator calls — subclasses may instrument the mutators).
     */
    public CharacteristicState(CharacteristicState source) {
        this.name = source.name;
        this.cardTypes.addAll(source.cardTypes);
        this.supertypes.addAll(source.supertypes);
        this.subtypes.addAll(source.subtypes);
        this.colors.addAll(source.colors);
        this.colorsOverridden = source.colorsOverridden;
        this.seededColors.addAll(source.seededColors);
        this.keywords.addAll(source.keywords);
        this.seededKeywords.addAll(source.seededKeywords);
        this.protectionColors.addAll(source.protectionColors);
        this.grantedActivatedAbilities.addAll(source.grantedActivatedAbilities);
        this.grantedStaticEffects.addAll(source.grantedStaticEffects);
        this.basePower = source.basePower;
        this.baseToughness = source.baseToughness;
        this.powerDelta = source.powerDelta;
        this.toughnessDelta = source.toughnessDelta;
        this.switchCount = source.switchCount;
        this.losesAllAbilities = source.losesAllAbilities;
        this.losesAllAbilitiesTimestamp = source.losesAllAbilitiesTimestamp;
        this.printedAbilitiesRemoved = source.printedAbilitiesRemoved;
    }

    // --- Layer 3 (text) ---

    public void setName(String name) {
        this.name = name;
    }

    // --- Layer 4 (types) ---

    public void addCardType(CardType type) {
        cardTypes.add(type);
    }

    public void removeCardType(CardType type) {
        cardTypes.remove(type);
    }

    public void addSupertype(CardSupertype supertype) {
        supertypes.add(supertype);
    }

    public void addSubtype(CardSubtype subtype) {
        if (!subtypes.contains(subtype)) {
            subtypes.add(subtype);
        }
    }

    /** "Becomes a [subtype]" override: replaces the existing subtypes (e.g. Lignify). */
    public void overrideSubtypes(Collection<CardSubtype> replacement) {
        subtypes.clear();
        for (CardSubtype subtype : replacement) {
            addSubtype(subtype);
        }
    }

    /**
     * Removes every subtype matching the filter. Type-setting effects use this to clear one
     * type class (creature types, land types) before adding the new type — the classification
     * of subtypes into classes lives in the engine, not here.
     */
    public void removeSubtypesIf(java.util.function.Predicate<CardSubtype> filter) {
        subtypes.removeIf(filter);
    }

    /**
     * Snapshots the seeded (pre-layer-5/6) colors and keywords so the query layer can later
     * distinguish granted characteristics from the baseline. Called by the layer engine once
     * seeding (constructor values plus engine-side legacy transient state) is complete.
     */
    public void snapshotSeededCharacteristics() {
        seededColors.clear();
        seededColors.addAll(colors);
        seededKeywords.clear();
        seededKeywords.addAll(keywords);
    }

    // --- Layer 5 (colors) ---

    public void addColor(CardColor color) {
        colors.add(color);
    }

    /**
     * Replaces the colors during seeding (legacy animation state that predates the layered
     * pass) WITHOUT marking a layer-5 override — {@link #isColorsOverridden()} must only report
     * actual layer-5 setting effects.
     */
    public void replaceSeedColors(Collection<CardColor> replacement) {
        colors.clear();
        colors.addAll(replacement);
    }

    /** "Becomes [color]" override: replaces the existing colors (e.g. Incite, Nim Deathmantle). */
    public void overrideColors(Collection<CardColor> replacement) {
        colors.clear();
        colors.addAll(replacement);
        this.colorsOverridden = true;
    }

    // --- Layer 6 (abilities) ---

    public void addKeyword(Keyword keyword) {
        keywords.add(keyword);
    }

    public void addKeywords(Collection<Keyword> granted) {
        keywords.addAll(granted);
    }

    public void removeKeyword(Keyword keyword) {
        keywords.remove(keyword);
    }

    public void addProtectionColors(Collection<CardColor> colors) {
        protectionColors.addAll(colors);
    }

    public void addActivatedAbility(ActivatedAbility ability) {
        grantedActivatedAbilities.add(ability);
    }

    public void addStaticEffect(CardEffect effect) {
        grantedStaticEffects.add(effect);
    }

    /**
     * Applies a "loses all abilities" effect: clears every ability accumulated so far (printed
     * and granted, including protection). Grants applied afterwards (later timestamps) stick.
     * The timestamp is kept so layer 7a can suppress the object's own CDAs that were removed
     * here.
     */
    public void loseAllAbilities(long timestamp) {
        keywords.clear();
        protectionColors.clear();
        grantedActivatedAbilities.clear();
        grantedStaticEffects.clear();
        this.losesAllAbilities = true;
        this.losesAllAbilitiesTimestamp = timestamp;
    }

    /**
     * Records that a layer-4 land-type-setting effect removed the object's printed abilities
     * (CR 305.7). The object's own static-slot effects stop existing from this point in the
     * pass; granted abilities and keywords are untouched (only rules-text abilities are lost).
     */
    public void removePrintedAbilities() {
        this.printedAbilitiesRemoved = true;
    }

    // --- Layer 7 ---

    public void addPowerToughnessDelta(int power, int toughness) {
        this.powerDelta += power;
        this.toughnessDelta += toughness;
    }

    /** Layer 7d: each switch is its own step; an even total cancels out. */
    public void switchPowerToughness() {
        this.switchCount++;
    }

    // --- Read accessors for the query layer ---

    public boolean hasCardType(CardType type) {
        return cardTypes.contains(type);
    }

    public boolean hasSubtype(CardSubtype subtype) {
        return subtypes.contains(subtype);
    }

    public boolean hasKeyword(Keyword keyword) {
        return keywords.contains(keyword);
    }

    /** Final power after all applied layers, honoring an odd number of 7d switches. */
    public int getEffectivePower() {
        return isSwitched() ? baseToughness + toughnessDelta : basePower + powerDelta;
    }

    /** Final toughness after all applied layers, honoring an odd number of 7d switches. */
    public int getEffectiveToughness() {
        return isSwitched() ? basePower + powerDelta : baseToughness + toughnessDelta;
    }

    public boolean isSwitched() {
        return switchCount % 2 != 0;
    }
}
