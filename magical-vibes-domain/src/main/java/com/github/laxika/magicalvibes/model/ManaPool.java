package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ManaPool {

    private final EnumMap<ManaColor, Integer> pool = new EnumMap<>(ManaColor.class);
    /** Snow mana is a tag on regular mana, like creature mana, and is not a separate mana type. */
    private final EnumMap<ManaColor, Integer> snowMana = new EnumMap<>(ManaColor.class);
    private boolean snowManaSpendableAsAnyColor;
    private final EnumMap<ManaColor, Integer> creatureMana = new EnumMap<>(ManaColor.class);
    /**
     * Mana that may only be spent to cast spells (e.g. mana from lands tapped via Piracy). Tracked as a
     * tag on a subset of the regular {@link #pool}, mirroring {@link #creatureMana}: spell casting draws
     * from the regular pool as usual, while ability activation temporarily withdraws this mana so it can't
     * pay ability costs.
     */
    private final EnumMap<ManaColor, Integer> spellOnlyMana = new EnumMap<>(ManaColor.class);
    /** Mana that may only be spent to pay ability costs, not to cast spells (e.g. Thran Turbine). */
    private final EnumMap<ManaColor, Integer> abilityOnlyMana = new EnumMap<>(ManaColor.class);
    /** Ability-only mana temporarily promoted into the regular pool during ability payment. */
    private final EnumMap<ManaColor, Integer> promotedAbilityOnlyMana = new EnumMap<>(ManaColor.class);
    /** Mana spendable only to cast artifact spells or activate any ability (Guidelight Optimizer). */
    private final EnumMap<ManaColor, Integer> artifactSpellOrAbilityOnlyMana = new EnumMap<>(ManaColor.class);
    /** Guidelight Optimizer mana temporarily promoted into the regular pool during ability payment. */
    private final EnumMap<ManaColor, Integer> promotedArtifactSpellOrAbilityOnlyMana = new EnumMap<>(ManaColor.class);
    /** Mana that doesn't drain at step/phase transitions until end of turn (e.g. Grand Warlord Radha). */
    private final EnumMap<ManaColor, Integer> persistentMana = new EnumMap<>(ManaColor.class);
    /**
     * Mana carrying the rider "if that mana is spent on a creature spell, it gains haste until end of
     * turn" (Generator Servant). Like {@link #creatureMana} this is a tag on a subset of the regular
     * {@link #pool} — the mana itself is unrestricted — and is never counted towards any total.
     * {@link #remove(ManaColor)} spends the tagged mana first, so the caster always gets the rider
     * when the pool holds both tagged and untagged mana of that color.
     */
    private final EnumMap<ManaColor, Integer> hasteGrantingMana = new EnumMap<>(ManaColor.class);
    /**
     * Mana carrying the rider "if that mana is spent on an instant or sorcery spell, that spell can't
     * be countered" (Boseiju, Who Shelters All). Like {@link #hasteGrantingMana} this is a tag on a
     * subset of the regular {@link #pool} — the mana itself is unrestricted — and is never counted
     * towards any total. {@link #remove(ManaColor)} spends the tagged mana first, so the caster always
     * gets the rider when the pool holds both tagged and untagged mana of that color.
     */
    private final EnumMap<ManaColor, Integer> uncounterableGrantingMana = new EnumMap<>(ManaColor.class);
    /** Mana carrying the rider "if spent on a multicolored creature spell, it enters with an additional +1/+1 counter". */
    private final EnumMap<ManaColor, Integer> additionalCounterGrantingMana = new EnumMap<>(ManaColor.class);
    /** Mana carrying the rider "if spent on a creature spell, it gains riot". */
    private final EnumMap<ManaColor, Integer> riotGrantingMana = new EnumMap<>(ManaColor.class);
    private int artifactOnlyColorless;
    /** Per-color mana spendable only to cast artifact spells or activate abilities of artifacts (Vedalken Engineer). */
    private final EnumMap<ManaColor, Integer> artifactOnlyMana = new EnumMap<>(ManaColor.class);
    /** Colorless mana spendable only to activate abilities of artifacts (Soldevi Machinist). */
    private int artifactAbilityOnlyColorless;
    /** Colorless mana that can't be spent to cast nonartifact spells (Powerstone tokens). */
    private int powerstoneOnlyColorless;
    private int myrOnlyColorless;
    /** Per-subtype colorless mana spendable only for matching spells or abilities. */
    private final Map<CardSubtype, Integer> colorlessSubtypeSpellOrAbilityMana = new HashMap<>();
    /** Colorless mana spendable only to cast legendary spells (Untaidake, the Cloud Keeper). */
    private int legendarySpellOnlyColorless;
    private int restrictedRed;
    private int kickedOnlyGreen;
    private int instantSorceryOnlyColorless;
    private int foretellOrInstantSorceryOnlyColorless;
    private int disturbOrInstantSorceryOnlyColorless;
    private int foretellSpellOnlyColorless;
    /** Colorless mana spendable only on costs that contain {X} (Rosheen Meanderer). */
    private int xCostOnlyColorless;
    /** Colorless mana spendable only to pay cumulative upkeep costs (Adarkar Unicorn, Snowfall). */
    private int cumulativeUpkeepOnlyColorless;
    /** Colored mana that can only be spent to cast instant or sorcery spells (e.g. Abstract Paintmage). */
    private final EnumMap<ManaColor, Integer> instantSorceryOnlyColored = new EnumMap<>(ManaColor.class);
    private final EnumMap<ManaColor, Integer> foretellOrInstantSorceryOnlyColored = new EnumMap<>(ManaColor.class);
    private final EnumMap<ManaColor, Integer> disturbOrInstantSorceryOnlyColored = new EnumMap<>(ManaColor.class);
    private final EnumMap<ManaColor, Integer> foretellSpellOnlyColored = new EnumMap<>(ManaColor.class);
    private final Map<CardSubtype, EnumMap<ManaColor, Integer>> subtypeOrLegendaryCreatureMana = new HashMap<>();
    /** Colored mana spendable only to pay cumulative upkeep costs (Adarkar Unicorn). */
    private final EnumMap<ManaColor, Integer> cumulativeUpkeepOnlyColored = new EnumMap<>(ManaColor.class);
    /** Per-color mana that can only be spent to cast spells with flashback from a graveyard (e.g. Altar of the Lost). */
    private final EnumMap<ManaColor, Integer> flashbackOnlyMana = new EnumMap<>(ManaColor.class);
    /** Per-color mana that can only be spent to cast spells from a graveyard. */
    private final EnumMap<ManaColor, Integer> graveyardOnlyMana = new EnumMap<>(ManaColor.class);
    /** Per-subtype, per-color mana that can only be spent to cast creature spells with a matching subtype (e.g. Pillar of Origins). */
    private final Map<CardSubtype, EnumMap<ManaColor, Integer>> subtypeCreatureMana = new HashMap<>();
    /**
     * The subset of {@link #subtypeCreatureMana} that also makes the spell it pays for uncounterable
     * (Cavern of Souls). Never counted towards any total — every entry here is already counted in
     * {@link #subtypeCreatureMana}; this map only records which of that mana carries the rider.
     */
    private final Map<CardSubtype, EnumMap<ManaColor, Integer>> uncounterableSubtypeCreatureMana = new HashMap<>();
    /**
     * Set when {@link #removeSubtypeCreatureMana} consumed uncounterable-granting mana, so the caster
     * can mark the spell being paid for uncounterable. Consumed (and reset) per payment.
     */
    private boolean spentUncounterableGrantingMana;
    /**
     * Per-subtype, per-color mana that can only be spent to cast spells with a matching subtype OR to
     * activate abilities of permanents with that subtype (e.g. Smokebraider). Distinct from
     * {@link #subtypeCreatureMana}, which is spell-only.
     */
    private final Map<CardSubtype, EnumMap<ManaColor, Integer>> subtypeSpellOrAbilityMana = new HashMap<>();
    /** Per-color mana that can only be spent to cast spells with any of the matching subtypes. */
    private final Map<Set<CardSubtype>, EnumMap<ManaColor, Integer>> subtypeSpellOnlyMana = new HashMap<>();
    /** Per-subtype, per-color mana spendable only for creature spells or creature-source abilities. */
    private final Map<CardSubtype, EnumMap<ManaColor, Integer>> subtypeCreatureSourceSpellOrAbilityMana = new HashMap<>();
    /** Per-restriction, per-color mana that can only be spent on a subtype or matching planeswalker spell. */
    private final Map<ManaRestriction.SubtypeOrPlaneswalkerSpells, EnumMap<ManaColor, Integer>> subtypeOrPlaneswalkerSpellMana = new HashMap<>();
    /** Mana spendable for any Cleric, Rogue, Warrior, or Wizard spell or ability (Base Camp). */
    private final EnumMap<ManaColor, Integer> partySpellOrAbilityMana = new EnumMap<>(ManaColor.class);
    /**
     * Per-color mana that can only be spent to cast a creature spell of any type (e.g. Ancient
     * Ziggurat). Distinct from {@link #subtypeCreatureMana}, which is restricted to one chosen
     * creature subtype; this bucket pays for every creature spell.
     */
    private final EnumMap<ManaColor, Integer> creatureSpellOnlyMana = new EnumMap<>(ManaColor.class);
    /** Per-color mana that can only be spent to cast creature or enchantment spells. */
    private final EnumMap<ManaColor, Integer> creatureOrEnchantmentSpellOnlyMana = new EnumMap<>(ManaColor.class);
    /** Temporary regular-pool tag used while paying a creature or enchantment spell. */
    /** Per-color mana that can only be spent to cast creature spells or activate abilities of creature sources (Gwenna, Eyes of Gaea). */
    private final EnumMap<ManaColor, Integer> creatureSpellOrAbilityMana = new EnumMap<>(ManaColor.class);
    /** Per-color mana that can only be spent to cast spells with mana value 4 or greater. */
    private final EnumMap<ManaColor, Integer> manaValueAtLeastFourOnlyMana = new EnumMap<>(ManaColor.class);
    /**
     * Per-exiled-card, per-color mana that may only be spent to cast that one exiled card (Ice
     * Cauldron — "spend this mana only to cast the last card exiled with this artifact"). Keyed by
     * the exiled card's id. Unlike the other restricted buckets this one has no {@link ManaCost}
     * spend path: {@code SpellCastingService.playCardFromExile} promotes the matching entry into the
     * regular pool for the duration of the payment and returns whatever it didn't spend.
     */
    private final Map<UUID, EnumMap<ManaColor, Integer>> exiledCardOnlyMana = new HashMap<>();
    /** Per-color mana that may only be spent to cast spells from exile. */
    private final EnumMap<ManaColor, Integer> exiledSpellOnlyMana = new EnumMap<>(ManaColor.class);
    /**
     * Permission flag (not mana): while set, white mana in this pool may additionally be spent to pay
     * red mana costs (Sunglasses of Urza — "you may spend white mana as though it were red mana"). Set
     * from board state at the payment/affordability sites; honored by {@link ManaCost#canPay}/{@code pay}.
     */
    private boolean whiteSpendableAsRed;
    /**
     * Permission flag (not mana): while set, white mana in this pool may be spent as mana of any
     * color, and every other mana may be spent only as though it were colorless (Celestial Dawn).
     * Set from board state at the payment/affordability sites; honored by
     * {@link ManaCost#canPay}/{@code pay}, which rewrite the pool accordingly before paying.
     */
    private boolean whiteSpendableAsAnyColor;
    /** Permission flag: white mana in this pool may additionally pay any colored requirement. */
    private boolean whiteSpendableAsAnyColorWithoutRestriction;
    /** Permission flag: all mana in this pool may be spent as though it were mana of any color. */
    private boolean allManaSpendableAsAnyColor;
    /** Permission flag (not mana): while set, blue mana in this pool may additionally pay any
     * colored requirement of an activated ability of the current source creature. */
    private boolean blueSpendableAsAnyColorForActivatedAbilities;
    /** Permission flag (not mana): while set, all mana in this pool may pay colored requirements
     * of an activated ability of the current source creature. */
    private boolean allManaSpendableAsAnyColorForActivatedAbilities;

    public ManaPool() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
            snowMana.put(color, 0);
            creatureMana.put(color, 0);
            spellOnlyMana.put(color, 0);
        abilityOnlyMana.put(color, 0);
        promotedAbilityOnlyMana.put(color, 0);
            artifactSpellOrAbilityOnlyMana.put(color, 0);
            promotedArtifactSpellOrAbilityOnlyMana.put(color, 0);
            persistentMana.put(color, 0);
            hasteGrantingMana.put(color, 0);
            uncounterableGrantingMana.put(color, 0);
            additionalCounterGrantingMana.put(color, 0);
            riotGrantingMana.put(color, 0);
            flashbackOnlyMana.put(color, 0);
            graveyardOnlyMana.put(color, 0);
            instantSorceryOnlyColored.put(color, 0);
            disturbOrInstantSorceryOnlyColored.put(color, 0);
            cumulativeUpkeepOnlyColored.put(color, 0);
            creatureSpellOnlyMana.put(color, 0);
            creatureSpellOrAbilityMana.put(color, 0);
            artifactOnlyMana.put(color, 0);
            partySpellOrAbilityMana.put(color, 0);
            exiledSpellOnlyMana.put(color, 0);
        }
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     */
    public ManaPool(ManaPool source) {
        pool.putAll(source.pool);
        snowMana.putAll(source.snowMana);
        creatureMana.putAll(source.creatureMana);
        spellOnlyMana.putAll(source.spellOnlyMana);
        abilityOnlyMana.putAll(source.abilityOnlyMana);
        promotedAbilityOnlyMana.putAll(source.promotedAbilityOnlyMana);
        artifactSpellOrAbilityOnlyMana.putAll(source.artifactSpellOrAbilityOnlyMana);
        promotedArtifactSpellOrAbilityOnlyMana.putAll(source.promotedArtifactSpellOrAbilityOnlyMana);
        persistentMana.putAll(source.persistentMana);
        hasteGrantingMana.putAll(source.hasteGrantingMana);
        uncounterableGrantingMana.putAll(source.uncounterableGrantingMana);
        additionalCounterGrantingMana.putAll(source.additionalCounterGrantingMana);
        riotGrantingMana.putAll(source.riotGrantingMana);
        flashbackOnlyMana.putAll(source.flashbackOnlyMana);
        graveyardOnlyMana.putAll(source.graveyardOnlyMana);
        this.artifactOnlyColorless = source.artifactOnlyColorless;
        artifactOnlyMana.putAll(source.artifactOnlyMana);
        this.artifactAbilityOnlyColorless = source.artifactAbilityOnlyColorless;
        this.powerstoneOnlyColorless = source.powerstoneOnlyColorless;
        this.myrOnlyColorless = source.myrOnlyColorless;
        colorlessSubtypeSpellOrAbilityMana.putAll(source.colorlessSubtypeSpellOrAbilityMana);
        this.legendarySpellOnlyColorless = source.legendarySpellOnlyColorless;
        this.restrictedRed = source.restrictedRed;
        this.kickedOnlyGreen = source.kickedOnlyGreen;
        this.instantSorceryOnlyColorless = source.instantSorceryOnlyColorless;
        this.foretellOrInstantSorceryOnlyColorless = source.foretellOrInstantSorceryOnlyColorless;
        this.disturbOrInstantSorceryOnlyColorless = source.disturbOrInstantSorceryOnlyColorless;
        this.foretellSpellOnlyColorless = source.foretellSpellOnlyColorless;
        this.xCostOnlyColorless = source.xCostOnlyColorless;
        this.cumulativeUpkeepOnlyColorless = source.cumulativeUpkeepOnlyColorless;
        instantSorceryOnlyColored.putAll(source.instantSorceryOnlyColored);
        foretellOrInstantSorceryOnlyColored.putAll(source.foretellOrInstantSorceryOnlyColored);
        disturbOrInstantSorceryOnlyColored.putAll(source.disturbOrInstantSorceryOnlyColored);
        foretellSpellOnlyColored.putAll(source.foretellSpellOnlyColored);
        for (Map.Entry<CardSubtype, EnumMap<ManaColor, Integer>> entry : source.subtypeOrLegendaryCreatureMana.entrySet()) {
            subtypeOrLegendaryCreatureMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        cumulativeUpkeepOnlyColored.putAll(source.cumulativeUpkeepOnlyColored);
        for (Map.Entry<CardSubtype, EnumMap<ManaColor, Integer>> entry : source.subtypeCreatureMana.entrySet()) {
            subtypeCreatureMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        for (Map.Entry<CardSubtype, EnumMap<ManaColor, Integer>> entry : source.uncounterableSubtypeCreatureMana.entrySet()) {
            uncounterableSubtypeCreatureMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        this.spentUncounterableGrantingMana = source.spentUncounterableGrantingMana;
        for (Map.Entry<CardSubtype, EnumMap<ManaColor, Integer>> entry : source.subtypeSpellOrAbilityMana.entrySet()) {
            subtypeSpellOrAbilityMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        for (Map.Entry<Set<CardSubtype>, EnumMap<ManaColor, Integer>> entry : source.subtypeSpellOnlyMana.entrySet()) {
            subtypeSpellOnlyMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        for (Map.Entry<CardSubtype, EnumMap<ManaColor, Integer>> entry : source.subtypeCreatureSourceSpellOrAbilityMana.entrySet()) {
            subtypeCreatureSourceSpellOrAbilityMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        for (Map.Entry<ManaRestriction.SubtypeOrPlaneswalkerSpells, EnumMap<ManaColor, Integer>> entry : source.subtypeOrPlaneswalkerSpellMana.entrySet()) {
            subtypeOrPlaneswalkerSpellMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        partySpellOrAbilityMana.putAll(source.partySpellOrAbilityMana);
        creatureSpellOnlyMana.putAll(source.creatureSpellOnlyMana);
        creatureOrEnchantmentSpellOnlyMana.putAll(source.creatureOrEnchantmentSpellOnlyMana);
        creatureSpellOrAbilityMana.putAll(source.creatureSpellOrAbilityMana);
        manaValueAtLeastFourOnlyMana.putAll(source.manaValueAtLeastFourOnlyMana);
        for (Map.Entry<UUID, EnumMap<ManaColor, Integer>> entry : source.exiledCardOnlyMana.entrySet()) {
            exiledCardOnlyMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        exiledSpellOnlyMana.putAll(source.exiledSpellOnlyMana);
        this.whiteSpendableAsRed = source.whiteSpendableAsRed;
        this.whiteSpendableAsAnyColor = source.whiteSpendableAsAnyColor;
        this.whiteSpendableAsAnyColorWithoutRestriction = source.whiteSpendableAsAnyColorWithoutRestriction;
        this.allManaSpendableAsAnyColor = source.allManaSpendableAsAnyColor;
        this.snowManaSpendableAsAnyColor = source.snowManaSpendableAsAnyColor;
        this.blueSpendableAsAnyColorForActivatedAbilities = source.blueSpendableAsAnyColorForActivatedAbilities;
        this.allManaSpendableAsAnyColorForActivatedAbilities = source.allManaSpendableAsAnyColorForActivatedAbilities;
    }

    /** See {@link #whiteSpendableAsRed}. */
    public boolean isWhiteSpendableAsRed() {
        return whiteSpendableAsRed;
    }

    /** See {@link #whiteSpendableAsRed}. */
    public void setWhiteSpendableAsRed(boolean whiteSpendableAsRed) {
        this.whiteSpendableAsRed = whiteSpendableAsRed;
    }

    /** See {@link #whiteSpendableAsAnyColor}. */
    public boolean isWhiteSpendableAsAnyColor() {
        return whiteSpendableAsAnyColor;
    }

    /** See {@link #whiteSpendableAsAnyColor}. */
    public void setWhiteSpendableAsAnyColor(boolean whiteSpendableAsAnyColor) {
        this.whiteSpendableAsAnyColor = whiteSpendableAsAnyColor;
    }

    public boolean isWhiteSpendableAsAnyColorWithoutRestriction() {
        return whiteSpendableAsAnyColorWithoutRestriction;
    }

    public void setWhiteSpendableAsAnyColorWithoutRestriction(boolean enabled) {
        this.whiteSpendableAsAnyColorWithoutRestriction = enabled;
    }

    public boolean isAllManaSpendableAsAnyColor() {
        return allManaSpendableAsAnyColor;
    }

    public void setAllManaSpendableAsAnyColor(boolean enabled) {
        this.allManaSpendableAsAnyColor = enabled;
    }

    public boolean isSnowManaSpendableAsAnyColor() {
        return snowManaSpendableAsAnyColor;
    }

    public void setSnowManaSpendableAsAnyColor(boolean enabled) {
        this.snowManaSpendableAsAnyColor = enabled;
    }

    public boolean isBlueSpendableAsAnyColorForActivatedAbilities() {
        return blueSpendableAsAnyColorForActivatedAbilities;
    }

    public void setBlueSpendableAsAnyColorForActivatedAbilities(boolean enabled) {
        this.blueSpendableAsAnyColorForActivatedAbilities = enabled;
    }

    public boolean isAllManaSpendableAsAnyColorForActivatedAbilities() {
        return allManaSpendableAsAnyColorForActivatedAbilities;
    }

    public void setAllManaSpendableAsAnyColorForActivatedAbilities(boolean enabled) {
        this.allManaSpendableAsAnyColorForActivatedAbilities = enabled;
    }

    public void add(ManaColor color) {
        pool.merge(color, 1, Integer::sum);
    }

    public void add(ManaColor color, int amount) {
        pool.merge(color, amount, Integer::sum);
    }

    /** Adds mana produced by a snow source. The mana keeps its normal color and gains the snow tag. */
    public void addSnowMana(ManaColor color, int amount) {
        add(color, amount);
        addSnowManaTag(color, amount);
    }

    /** Copies a snow tag onto mana already present in this pool. */
    public void addSnowManaTag(ManaColor color, int amount) {
        snowMana.merge(color, amount, Integer::sum);
    }

    public int getSnowMana(ManaColor color) {
        return snowMana.getOrDefault(color, 0);
    }

    public int getSnowManaTotal() {
        return snowMana.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Returns a defensive snapshot of the snow tags currently available by color. */
    public EnumMap<ManaColor, Integer> getSnowManaTotals() {
        return new EnumMap<>(snowMana);
    }

    /** Changes the color a snow mana may be spent as while retaining its snow tag. */
    public boolean convertSnowMana(ManaColor from, ManaColor to) {
        if (from == to || getSnowMana(from) <= 0) {
            return false;
        }
        remove(from);
        add(to);
        addSnowManaTag(to, 1);
        return true;
    }

    /** Spends one snow mana of the given color, if available. */
    public void removeSnowMana(ManaColor color) {
        if (getSnowMana(color) > 0) {
            remove(color);
        }
    }

    /** Spends up to {@code amount} snow mana, preserving the ordinary mana payment API. */
    public void removeSnowMana(int amount) {
        for (ManaColor color : ManaColor.values()) {
            int toRemove = Math.min(amount, getSnowMana(color));
            for (int i = 0; i < toRemove; i++) {
                removeSnowMana(color);
            }
            amount -= toRemove;
            if (amount == 0) {
                return;
            }
        }
    }

    public void clear() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
            snowMana.put(color, 0);
            creatureMana.put(color, 0);
            spellOnlyMana.put(color, 0);
            abilityOnlyMana.put(color, 0);
            promotedAbilityOnlyMana.put(color, 0);
            artifactSpellOrAbilityOnlyMana.put(color, 0);
            promotedArtifactSpellOrAbilityOnlyMana.put(color, 0);
            hasteGrantingMana.put(color, 0);
            uncounterableGrantingMana.put(color, 0);
            additionalCounterGrantingMana.put(color, 0);
            riotGrantingMana.put(color, 0);
            flashbackOnlyMana.put(color, 0);
            graveyardOnlyMana.put(color, 0);
        }
        artifactOnlyColorless = 0;
        for (ManaColor color : ManaColor.values()) {
            artifactOnlyMana.put(color, 0);
        }
        artifactAbilityOnlyColorless = 0;
        powerstoneOnlyColorless = 0;
        myrOnlyColorless = 0;
        colorlessSubtypeSpellOrAbilityMana.clear();
        legendarySpellOnlyColorless = 0;
        restrictedRed = 0;
        kickedOnlyGreen = 0;
        instantSorceryOnlyColorless = 0;
        foretellOrInstantSorceryOnlyColorless = 0;
        disturbOrInstantSorceryOnlyColorless = 0;
        foretellSpellOnlyColorless = 0;
        xCostOnlyColorless = 0;
        cumulativeUpkeepOnlyColorless = 0;
        for (ManaColor color : ManaColor.values()) {
            instantSorceryOnlyColored.put(color, 0);
            foretellOrInstantSorceryOnlyColored.put(color, 0);
            disturbOrInstantSorceryOnlyColored.put(color, 0);
            foretellSpellOnlyColored.put(color, 0);
            cumulativeUpkeepOnlyColored.put(color, 0);
            creatureSpellOnlyMana.put(color, 0);
            creatureSpellOrAbilityMana.put(color, 0);
            manaValueAtLeastFourOnlyMana.put(color, 0);
        }
        subtypeCreatureMana.clear();
        uncounterableSubtypeCreatureMana.clear();
        spentUncounterableGrantingMana = false;
        subtypeSpellOrAbilityMana.clear();
        subtypeSpellOnlyMana.clear();
        subtypeCreatureSourceSpellOrAbilityMana.clear();
        subtypeOrPlaneswalkerSpellMana.clear();
        subtypeOrLegendaryCreatureMana.clear();
        partySpellOrAbilityMana.replaceAll((color, amount) -> 0);
        exiledCardOnlyMana.clear();
        exiledSpellOnlyMana.replaceAll((color, amount) -> 0);
    }

    public int get(ManaColor color) {
        return pool.getOrDefault(color, 0);
    }

    public int getTotal() {
        int total = 0;
        for (int value : pool.values()) {
            total += value;
        }
        return total;
    }

    /**
     * Inflation of {@link #getTotal()} from sources whose mana abilities are mutually
     * exclusive (only one ability activates per tap). Always 0 for a plain pool;
     * overridden by {@link VirtualManaPool}.
     */
    public int getFlexibleOvercount() {
        return 0;
    }

    /**
     * Inflation of {@link #get(ManaColor)} for the given color from a single source
     * with multiple abilities producing that color. Always 0 for a plain pool;
     * overridden by {@link VirtualManaPool}.
     */
    public int getPerColorOvercount(ManaColor color) {
        return 0;
    }

    /**
     * Total mana available across all pool buckets (regular, restricted, flashback-only, etc.).
     * Used to snapshot mana before/after spell payment to compute mana spent.
     */
    public int getTotalAllMana() {
        // NOTE: creatureMana and persistentMana are tags on a subset of the regular pool, not
        // separate buckets, so they are already counted by getTotal() and must not be added again.
        int total = getTotal();
        total += getAbilityOnlyManaTotal();
        total += getArtifactSpellOrAbilityOnlyManaTotal();
        total += artifactOnlyColorless;
        total += getArtifactOnlyManaTotal();
        total += artifactAbilityOnlyColorless;
        total += powerstoneOnlyColorless;
        total += myrOnlyColorless;
        total += colorlessSubtypeSpellOrAbilityMana.values().stream().mapToInt(Integer::intValue).sum();
        total += legendarySpellOnlyColorless;
        total += restrictedRed;
        total += kickedOnlyGreen;
        total += instantSorceryOnlyColorless;
        total += foretellOrInstantSorceryOnlyColorless;
        total += disturbOrInstantSorceryOnlyColorless;
        total += foretellSpellOnlyColorless;
        for (int value : instantSorceryOnlyColored.values()) {
            total += value;
        }
        for (int value : foretellOrInstantSorceryOnlyColored.values()) {
            total += value;
        }
        total += getForetellSpellOnlyColoredTotal();
        for (int value : disturbOrInstantSorceryOnlyColored.values()) {
            total += value;
        }
        total += xCostOnlyColorless;
        total += cumulativeUpkeepOnlyColorless;
        total += getCumulativeUpkeepOnlyColoredTotal();
        total += getFlashbackOnlyManaTotal();
        total += getGraveyardOnlyManaTotal();
        for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        for (EnumMap<ManaColor, Integer> colorMap : subtypeOrLegendaryCreatureMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOrAbilityMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOnlyMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureSourceSpellOrAbilityMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        for (EnumMap<ManaColor, Integer> colorMap : subtypeOrPlaneswalkerSpellMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        for (int value : partySpellOrAbilityMana.values()) {
            total += value;
        }
        total += getCreatureSpellOnlyManaTotal();
        total += getCreatureOrEnchantmentSpellOnlyManaTotal();
        total += getCreatureSpellOrAbilityManaTotal();
        total += getManaValueAtLeastFourOnlyManaTotal();
        for (EnumMap<ManaColor, Integer> colorMap : exiledCardOnlyMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        total += getExiledSpellOnlyManaTotal();
        return total;
    }

    /**
     * Adds mana spendable only to cast the exiled card {@code exiledCardId} (Ice Cauldron).
     * See {@link #exiledCardOnlyMana}.
     */
    public void addExiledCardOnlyMana(UUID exiledCardId, ManaColor color, int amount) {
        exiledCardOnlyMana
                .computeIfAbsent(exiledCardId, id -> new EnumMap<>(ManaColor.class))
                .merge(color, amount, Integer::sum);
    }

    /** The mana reserved for casting {@code exiledCardId}, per color (empty if none). */
    public Map<ManaColor, Integer> getExiledCardOnlyMana(UUID exiledCardId) {
        EnumMap<ManaColor, Integer> reserved = exiledCardOnlyMana.get(exiledCardId);
        return reserved != null ? new EnumMap<>(reserved) : new EnumMap<>(ManaColor.class);
    }

    /**
     * Moves the mana reserved for {@code exiledCardId} into the regular pool so the normal payment
     * path can spend it, and returns the amounts moved. The caller must hand the unspent remainder
     * back with {@link #returnExiledCardOnlyMana} so it doesn't become unrestricted mana.
     */
    public Map<ManaColor, Integer> promoteExiledCardOnlyMana(UUID exiledCardId) {
        EnumMap<ManaColor, Integer> reserved = exiledCardOnlyMana.remove(exiledCardId);
        if (reserved == null) {
            return new EnumMap<>(ManaColor.class);
        }
        for (Map.Entry<ManaColor, Integer> entry : reserved.entrySet()) {
            pool.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        return reserved;
    }

    /**
     * Withdraws {@code amounts} from the regular pool back into the reserved bucket for
     * {@code exiledCardId} — the inverse of {@link #promoteExiledCardOnlyMana} for whatever the
     * payment left behind.
     */
    public void returnExiledCardOnlyMana(UUID exiledCardId, Map<ManaColor, Integer> amounts) {
        for (Map.Entry<ManaColor, Integer> entry : amounts.entrySet()) {
            int amount = entry.getValue();
            if (amount <= 0) {
                continue;
            }
            pool.merge(entry.getKey(), -amount, Integer::sum);
            addExiledCardOnlyMana(exiledCardId, entry.getKey(), amount);
        }
    }

    /** Adds mana spendable only to cast a spell from exile. */
    public void addExiledSpellOnlyMana(ManaColor color, int amount) {
        exiledSpellOnlyMana.merge(color, amount, Integer::sum);
    }

    public int getExiledSpellOnlyMana(ManaColor color) {
        return exiledSpellOnlyMana.getOrDefault(color, 0);
    }

    public int getExiledSpellOnlyManaTotal() {
        int total = 0;
        for (int value : exiledSpellOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    /** Moves exile-only mana into the regular pool for a spell cast from exile. */
    public EnumMap<ManaColor, Integer> promoteExiledSpellOnlyMana() {
        EnumMap<ManaColor, Integer> promoted = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            int amount = exiledSpellOnlyMana.getOrDefault(color, 0);
            if (amount > 0) {
                promoted.put(color, amount);
                pool.merge(color, amount, Integer::sum);
                exiledSpellOnlyMana.put(color, 0);
            }
        }
        return promoted;
    }

    /** Returns unspent exile-only mana to its restricted bucket after an exile cast. */
    public void returnExiledSpellOnlyMana(Map<ManaColor, Integer> amounts) {
        for (Map.Entry<ManaColor, Integer> entry : amounts.entrySet()) {
            int amount = entry.getValue();
            if (amount <= 0) {
                continue;
            }
            pool.merge(entry.getKey(), -amount, Integer::sum);
            exiledSpellOnlyMana.merge(entry.getKey(), amount, Integer::sum);
        }
    }

    /**
     * Total mana available per color across every bucket, colorless included. Used to snapshot the
     * pool before and after a payment so the exact types and amounts spent can be noted.
     */
    public EnumMap<ManaColor, Integer> getAllManaTotals() {
        EnumMap<ManaColor, Integer> totals = getColoredManaTotals();
        int colored = 0;
        for (int value : totals.values()) {
            colored += value;
        }
        totals.put(ManaColor.COLORLESS, getTotalAllMana() - colored);
        return totals;
    }

    public void remove(ManaColor color) {
        pool.merge(color, -1, Integer::sum);
        int snow = snowMana.getOrDefault(color, 0);
        if (snow > 0) {
            snowMana.put(color, snow - 1);
        }
        int promotedAbilityOnly = promotedAbilityOnlyMana.getOrDefault(color, 0);
        if (promotedAbilityOnly > 0) {
            promotedAbilityOnlyMana.put(color, promotedAbilityOnly - 1);
        } else {
            int promotedArtifactSpellOrAbilityOnly = promotedArtifactSpellOrAbilityOnlyMana.getOrDefault(color, 0);
            if (promotedArtifactSpellOrAbilityOnly > 0) {
                promotedArtifactSpellOrAbilityOnlyMana.put(color, promotedArtifactSpellOrAbilityOnly - 1);
            }
        }
        // Haste-granting mana (Generator Servant) is spent before untagged mana of the same color.
        int hasteGranting = hasteGrantingMana.getOrDefault(color, 0);
        if (hasteGranting > 0) {
            hasteGrantingMana.put(color, hasteGranting - 1);
        }
        // Uncounterable-granting mana (Boseiju, Who Shelters All) likewise goes first.
        int uncounterableGranting = uncounterableGrantingMana.getOrDefault(color, 0);
        if (uncounterableGranting > 0) {
            uncounterableGrantingMana.put(color, uncounterableGranting - 1);
        }
        int additionalCounterGranting = additionalCounterGrantingMana.getOrDefault(color, 0);
        if (additionalCounterGranting > 0) {
            additionalCounterGrantingMana.put(color, additionalCounterGranting - 1);
        }
        int riotGranting = riotGrantingMana.getOrDefault(color, 0);
        if (riotGranting > 0) {
            riotGrantingMana.put(color, riotGranting - 1);
        }
        // Clamp creature mana so it never exceeds total for this color
        int total = pool.getOrDefault(color, 0);
        int creature = creatureMana.getOrDefault(color, 0);
        if (creature > total) {
            creatureMana.put(color, total);
        }
        int spellOnly = spellOnlyMana.getOrDefault(color, 0);
        if (spellOnly > total) {
            spellOnlyMana.put(color, total);
        }
        if (hasteGrantingMana.getOrDefault(color, 0) > total) {
            hasteGrantingMana.put(color, total);
        }
        if (uncounterableGrantingMana.getOrDefault(color, 0) > total) {
            uncounterableGrantingMana.put(color, total);
        }
        if (additionalCounterGrantingMana.getOrDefault(color, 0) > total) {
            additionalCounterGrantingMana.put(color, total);
        }
        if (riotGrantingMana.getOrDefault(color, 0) > total) {
            riotGrantingMana.put(color, total);
        }
    }

    /**
     * Adds mana carrying the "spent on a creature spell → it gains haste" rider (Generator Servant).
     * The caller must also add the same amount to the regular pool via {@link #add(ManaColor, int)};
     * this only records the rider tag on that subset.
     */
    public void addHasteGrantingMana(ManaColor color, int amount) {
        hasteGrantingMana.merge(color, amount, Integer::sum);
    }

    /** Total tagged haste-granting mana still in the pool, across all colors. */
    public int getHasteGrantingManaTotal() {
        int total = 0;
        for (int value : hasteGrantingMana.values()) {
            total += value;
        }
        return total;
    }

    public int getHasteGrantingMana(ManaColor color) {
        return hasteGrantingMana.getOrDefault(color, 0);
    }

    /**
     * Adds mana carrying the "spent on an instant or sorcery spell → that spell can't be countered"
     * rider (Boseiju, Who Shelters All). The caller must also add the same amount to the regular pool
     * via {@link #add(ManaColor, int)}; this only records the rider tag on that subset.
     */
    public void addUncounterableGrantingMana(ManaColor color, int amount) {
        uncounterableGrantingMana.merge(color, amount, Integer::sum);
    }

    /** Total tagged uncounterable-granting mana still in the pool, across all colors. */
    public int getUncounterableGrantingManaTotal() {
        int total = 0;
        for (int value : uncounterableGrantingMana.values()) {
            total += value;
        }
        return total;
    }

    public int getUncounterableGrantingMana(ManaColor color) {
        return uncounterableGrantingMana.getOrDefault(color, 0);
    }

    /** Adds mana carrying the "spent on a multicolored creature spell -> additional +1/+1 counter" rider. */
    public void addAdditionalCounterGrantingMana(ManaColor color, int amount) {
        additionalCounterGrantingMana.merge(color, amount, Integer::sum);
    }

    /** Total mana still carrying the additional-counter rider, across all colors. */
    public int getAdditionalCounterGrantingManaTotal() {
        int total = 0;
        for (int value : additionalCounterGrantingMana.values()) {
            total += value;
        }
        return total;
    }

    /** Adds mana carrying the "spent on a creature spell -> it gains riot" rider. */
    public void addRiotGrantingMana(ManaColor color, int amount) {
        riotGrantingMana.merge(color, amount, Integer::sum);
    }

    /** Total mana still carrying the riot rider, across all colors. */
    public int getRiotGrantingManaTotal() {
        int total = 0;
        for (int value : riotGrantingMana.values()) {
            total += value;
        }
        return total;
    }

    /**
     * Adds spell-only mana (Piracy). The caller must also add the same amount to the regular pool via
     * {@link #add(ManaColor, int)}; this only records the spell-only tag on that subset.
     */
    public void addSpellOnlyMana(ManaColor color, int amount) {
        spellOnlyMana.merge(color, amount, Integer::sum);
    }

    public int getSpellOnlyMana(ManaColor color) {
        return spellOnlyMana.getOrDefault(color, 0);
    }

    public int getSpellOnlyManaTotal() {
        int total = 0;
        for (int value : spellOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    /**
     * Temporarily removes all spell-only mana from the pool (both the regular-pool subset and the tag),
     * returning the withdrawn amounts per color. Used to hide spell-only mana while paying an activated
     * ability's cost, since that mana may only be spent to cast spells. Restore with
     * {@link #restoreSpellOnlyMana(Map)}.
     */
    public Map<ManaColor, Integer> withdrawSpellOnlyMana() {
        EnumMap<ManaColor, Integer> withdrawn = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            int amount = spellOnlyMana.getOrDefault(color, 0);
            if (amount > 0) {
                withdrawn.put(color, amount);
                pool.merge(color, -amount, Integer::sum);
                spellOnlyMana.put(color, 0);
                int total = pool.getOrDefault(color, 0);
                if (creatureMana.getOrDefault(color, 0) > total) {
                    creatureMana.put(color, total);
                }
            }
        }
        return withdrawn;
    }

    /** Re-adds mana previously removed by {@link #withdrawSpellOnlyMana()}. */
    public void restoreSpellOnlyMana(Map<ManaColor, Integer> withdrawn) {
        for (Map.Entry<ManaColor, Integer> entry : withdrawn.entrySet()) {
            pool.merge(entry.getKey(), entry.getValue(), Integer::sum);
            spellOnlyMana.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }

    public int getAbilityOnlyMana(ManaColor color) {
        return abilityOnlyMana.getOrDefault(color, 0);
    }

    public int getAbilityOnlyManaTotal() {
        int total = 0;
        for (int value : abilityOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    /** Adds mana that can only be spent to pay an ability cost. */
    public void addAbilityOnlyMana(ManaColor color, int amount) {
        abilityOnlyMana.merge(color, amount, Integer::sum);
    }

    public void removeAbilityOnlyMana(ManaColor color, int amount) {
        int current = abilityOnlyMana.getOrDefault(color, 0);
        abilityOnlyMana.put(color, Math.max(0, current - amount));
    }

    /** Temporarily makes ability-only mana visible to the ordinary mana payment code. */
    public int promoteAbilityOnlyMana() {
        int promoted = 0;
        for (ManaColor color : ManaColor.values()) {
            int amount = abilityOnlyMana.getOrDefault(color, 0);
            if (amount > 0) {
                pool.merge(color, amount, Integer::sum);
                abilityOnlyMana.put(color, 0);
                promotedAbilityOnlyMana.merge(color, amount, Integer::sum);
                promoted += amount;
            }
            int artifactSpellOrAbilityAmount = artifactSpellOrAbilityOnlyMana.getOrDefault(color, 0);
            if (artifactSpellOrAbilityAmount > 0) {
                pool.merge(color, artifactSpellOrAbilityAmount, Integer::sum);
                artifactSpellOrAbilityOnlyMana.put(color, 0);
                promotedArtifactSpellOrAbilityOnlyMana.merge(color, artifactSpellOrAbilityAmount, Integer::sum);
                promoted += artifactSpellOrAbilityAmount;
            }
        }
        return promoted;
    }

    /** Returns unused promoted ability-only mana to its restricted bucket. */
    public void restorePromotedAbilityOnlyMana() {
        for (ManaColor color : ManaColor.values()) {
            int amount = promotedAbilityOnlyMana.getOrDefault(color, 0);
            if (amount > 0) {
                int returned = Math.min(amount, pool.getOrDefault(color, 0));
                pool.merge(color, -returned, Integer::sum);
                abilityOnlyMana.merge(color, returned, Integer::sum);
            }
            promotedAbilityOnlyMana.put(color, 0);

            int artifactSpellOrAbilityAmount = promotedArtifactSpellOrAbilityOnlyMana.getOrDefault(color, 0);
            if (artifactSpellOrAbilityAmount > 0) {
                int returned = Math.min(artifactSpellOrAbilityAmount, pool.getOrDefault(color, 0));
                pool.merge(color, -returned, Integer::sum);
                artifactSpellOrAbilityOnlyMana.merge(color, returned, Integer::sum);
            }
            promotedArtifactSpellOrAbilityOnlyMana.put(color, 0);
        }
    }

    public int getArtifactSpellOrAbilityOnlyMana(ManaColor color) {
        return artifactSpellOrAbilityOnlyMana.getOrDefault(color, 0);
    }

    public int getArtifactSpellOrAbilityOnlyManaTotal() {
        int total = 0;
        for (int value : artifactSpellOrAbilityOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    /** Adds mana spendable only for artifact spells or any activated ability. */
    public void addArtifactSpellOrAbilityOnlyMana(ManaColor color, int amount) {
        artifactSpellOrAbilityOnlyMana.merge(color, amount, Integer::sum);
    }

    public void removeArtifactSpellOrAbilityOnlyMana(ManaColor color, int amount) {
        int current = artifactSpellOrAbilityOnlyMana.getOrDefault(color, 0);
        artifactSpellOrAbilityOnlyMana.put(color, Math.max(0, current - amount));
    }

    public void addCreatureMana(ManaColor color, int amount) {
        creatureMana.merge(color, amount, Integer::sum);
    }

    /** Removes the creature-mana tag for the given color (floored at 0), e.g. when a
     *  mana-creature tap is reverted. The regular pool is unaffected. */
    public void removeCreatureMana(ManaColor color, int amount) {
        creatureMana.merge(color, -amount, Integer::sum);
        if (creatureMana.getOrDefault(color, 0) < 0) {
            creatureMana.put(color, 0);
        }
    }

    public int getCreatureMana(ManaColor color) {
        return creatureMana.getOrDefault(color, 0);
    }

    public int getCreatureManaTotal() {
        int total = 0;
        for (int value : creatureMana.values()) {
            total += value;
        }
        return total;
    }

    public int getArtifactOnlyColorless() {
        return artifactOnlyColorless;
    }

    public void addArtifactOnlyColorless(int amount) {
        artifactOnlyColorless += amount;
    }

    public void removeArtifactOnlyColorless(int amount) {
        artifactOnlyColorless = Math.max(0, artifactOnlyColorless - amount);
    }

    public int getArtifactOnlyMana(ManaColor color) {
        return artifactOnlyMana.getOrDefault(color, 0);
    }

    public int getArtifactOnlyManaTotal() {
        int total = 0;
        for (int value : artifactOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    public void addArtifactOnlyMana(ManaColor color, int amount) {
        artifactOnlyMana.merge(color, amount, Integer::sum);
    }

    public void removeArtifactOnlyMana(ManaColor color, int amount) {
        int current = artifactOnlyMana.getOrDefault(color, 0);
        artifactOnlyMana.put(color, Math.max(0, current - amount));
    }

    public int getArtifactAbilityOnlyColorless() {
        return artifactAbilityOnlyColorless;
    }

    public void addArtifactAbilityOnlyColorless(int amount) {
        artifactAbilityOnlyColorless += amount;
    }

    public void removeArtifactAbilityOnlyColorless(int amount) {
        artifactAbilityOnlyColorless = Math.max(0, artifactAbilityOnlyColorless - amount);
    }

    public int getPowerstoneOnlyColorless() {
        return powerstoneOnlyColorless;
    }

    public void addPowerstoneOnlyColorless(int amount) {
        powerstoneOnlyColorless += amount;
    }

    public void removePowerstoneOnlyColorless(int amount) {
        powerstoneOnlyColorless = Math.max(0, powerstoneOnlyColorless - amount);
    }

    public int getMyrOnlyColorless() {
        return myrOnlyColorless;
    }

    public void addMyrOnlyColorless(int amount) {
        myrOnlyColorless += amount;
    }

    public void removeMyrOnlyColorless(int amount) {
        myrOnlyColorless = Math.max(0, myrOnlyColorless - amount);
    }

    public int getColorlessSubtypeSpellOrAbilityMana(CardSubtype subtype) {
        return colorlessSubtypeSpellOrAbilityMana.getOrDefault(subtype, 0);
    }

    public void addColorlessSubtypeSpellOrAbilityMana(CardSubtype subtype, int amount) {
        colorlessSubtypeSpellOrAbilityMana.merge(subtype, amount, Integer::sum);
    }

    public void removeColorlessSubtypeSpellOrAbilityMana(CardSubtype subtype, int amount) {
        int current = colorlessSubtypeSpellOrAbilityMana.getOrDefault(subtype, 0);
        colorlessSubtypeSpellOrAbilityMana.put(subtype, Math.max(0, current - amount));
    }

    public int getLegendarySpellOnlyColorless() {
        return legendarySpellOnlyColorless;
    }

    public void addLegendarySpellOnlyColorless(int amount) {
        legendarySpellOnlyColorless += amount;
    }

    public void removeLegendarySpellOnlyColorless(int amount) {
        legendarySpellOnlyColorless = Math.max(0, legendarySpellOnlyColorless - amount);
    }

    public int getRestrictedRed() {
        return restrictedRed;
    }

    public void addRestrictedRed(int amount) {
        restrictedRed += amount;
    }

    public void removeRestrictedRed(int amount) {
        restrictedRed = Math.max(0, restrictedRed - amount);
    }

    public int getKickedOnlyGreen() {
        return kickedOnlyGreen;
    }

    public void addKickedOnlyGreen(int amount) {
        kickedOnlyGreen += amount;
    }

    public void removeKickedOnlyGreen(int amount) {
        kickedOnlyGreen = Math.max(0, kickedOnlyGreen - amount);
    }

    public void addFlashbackOnlyMana(ManaColor color, int amount) {
        flashbackOnlyMana.merge(color, amount, Integer::sum);
    }

    public int getFlashbackOnlyMana(ManaColor color) {
        return flashbackOnlyMana.getOrDefault(color, 0);
    }

    public int getFlashbackOnlyManaTotal() {
        int total = 0;
        for (int value : flashbackOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    public void removeFlashbackOnlyMana(ManaColor color, int amount) {
        int current = flashbackOnlyMana.getOrDefault(color, 0);
        flashbackOnlyMana.put(color, Math.max(0, current - amount));
    }

    public void addGraveyardOnlyMana(ManaColor color, int amount) {
        graveyardOnlyMana.merge(color, amount, Integer::sum);
    }

    public int getGraveyardOnlyMana(ManaColor color) {
        return graveyardOnlyMana.getOrDefault(color, 0);
    }

    public int getGraveyardOnlyManaTotal() {
        return graveyardOnlyMana.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void removeGraveyardOnlyMana(ManaColor color, int amount) {
        int current = graveyardOnlyMana.getOrDefault(color, 0);
        graveyardOnlyMana.put(color, Math.max(0, current - amount));
    }

    /** Temporarily exposes graveyard-only mana to the ordinary spell-payment algorithm. */
    public GraveyardOnlyManaState promoteGraveyardOnlyMana() {
        EnumMap<ManaColor, Integer> regularBefore = new EnumMap<>(ManaColor.class);
        EnumMap<ManaColor, Integer> promoted = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            regularBefore.put(color, get(color));
            int amount = getGraveyardOnlyMana(color);
            promoted.put(color, amount);
            if (amount > 0) {
                pool.merge(color, amount, Integer::sum);
                graveyardOnlyMana.put(color, 0);
            }
        }
        return new GraveyardOnlyManaState(regularBefore, promoted);
    }

    /** Restores unspent graveyard-only mana after a spell payment. */
    public void restorePromotedGraveyardOnlyMana(GraveyardOnlyManaState state) {
        for (ManaColor color : ManaColor.values()) {
            int promoted = state.promoted().getOrDefault(color, 0);
            int spent = Math.max(0, state.regularBefore().getOrDefault(color, 0)
                    + promoted - get(color));
            int remaining = Math.max(0, promoted - spent);
            if (remaining > 0) {
                pool.merge(color, -remaining, Integer::sum);
                graveyardOnlyMana.merge(color, remaining, Integer::sum);
            }
        }
    }

    public record GraveyardOnlyManaState(Map<ManaColor, Integer> regularBefore,
                                         Map<ManaColor, Integer> promoted) {
    }

    public int getInstantSorceryOnlyColorless() {
        return instantSorceryOnlyColorless + foretellOrInstantSorceryOnlyColorless
                + disturbOrInstantSorceryOnlyColorless;
    }

    public void addForetellOrInstantSorceryOnlyColorless(int amount) {
        foretellOrInstantSorceryOnlyColorless += amount;
    }

    public int getForetellOrInstantSorceryOnlyColorless() {
        return foretellOrInstantSorceryOnlyColorless;
    }

    public void removeForetellOrInstantSorceryOnlyColorless(int amount) {
        foretellOrInstantSorceryOnlyColorless = Math.max(0, foretellOrInstantSorceryOnlyColorless - amount);
    }

    public void addDisturbOrInstantSorceryOnlyColorless(int amount) {
        disturbOrInstantSorceryOnlyColorless += amount;
    }

    public int getDisturbOrInstantSorceryOnlyColorless() {
        return disturbOrInstantSorceryOnlyColorless;
    }

    public void removeDisturbOrInstantSorceryOnlyColorless(int amount) {
        disturbOrInstantSorceryOnlyColorless = Math.max(0, disturbOrInstantSorceryOnlyColorless - amount);
    }

    public void addInstantSorceryOnlyColorless(int amount) {
        instantSorceryOnlyColorless += amount;
    }

    public void removeInstantSorceryOnlyColorless(int amount) {
        int fromDisturb = Math.min(amount, disturbOrInstantSorceryOnlyColorless);
        disturbOrInstantSorceryOnlyColorless -= fromDisturb;
        amount -= fromDisturb;
        int fromForetell = Math.min(amount, foretellOrInstantSorceryOnlyColorless);
        foretellOrInstantSorceryOnlyColorless -= fromForetell;
        amount -= fromForetell;
        instantSorceryOnlyColorless = Math.max(0, instantSorceryOnlyColorless - amount);
    }

    public int getForetellSpellOnlyColorless() {
        return foretellSpellOnlyColorless;
    }

    public void addForetellSpellOnlyColorless(int amount) {
        foretellSpellOnlyColorless += amount;
    }

    public void removeForetellSpellOnlyColorless(int amount) {
        foretellSpellOnlyColorless = Math.max(0, foretellSpellOnlyColorless - amount);
    }

    public void addForetellSpellOnlyMana(ManaColor color, int amount) {
        if (color == ManaColor.COLORLESS) {
            addForetellSpellOnlyColorless(amount);
        } else {
            addForetellSpellOnlyColored(color, amount);
        }
    }

    public int getForetellSpellOnlyColored(ManaColor color) {
        return foretellSpellOnlyColored.getOrDefault(color, 0);
    }

    public int getForetellSpellOnlyColoredTotal() {
        int total = 0;
        for (int value : foretellSpellOnlyColored.values()) {
            total += value;
        }
        return total;
    }

    public int getForetellSpellOnlyManaTotal() {
        return foretellSpellOnlyColorless + getForetellSpellOnlyColoredTotal();
    }

    public void addForetellSpellOnlyColored(ManaColor color, int amount) {
        foretellSpellOnlyColored.merge(color, amount, Integer::sum);
    }

    public void removeForetellSpellOnlyColored(ManaColor color, int amount) {
        int current = foretellSpellOnlyColored.getOrDefault(color, 0);
        foretellSpellOnlyColored.put(color, Math.max(0, current - amount));
    }

    /** Temporarily exposes foretell-only mana to the ordinary spell payment algorithm. */
    public ForetellSpellOnlyManaState promoteForetellSpellOnlyMana() {
        EnumMap<ManaColor, Integer> regularBefore = new EnumMap<>(ManaColor.class);
        EnumMap<ManaColor, Integer> promoted = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            regularBefore.put(color, pool.getOrDefault(color, 0));
            int amount = color == ManaColor.COLORLESS
                    ? foretellSpellOnlyColorless
                    : foretellSpellOnlyColored.getOrDefault(color, 0);
            promoted.put(color, amount);
            if (amount > 0) {
                pool.merge(color, amount, Integer::sum);
            }
        }
        foretellSpellOnlyColorless = 0;
        foretellSpellOnlyColored.replaceAll((color, amount) -> 0);
        return new ForetellSpellOnlyManaState(regularBefore, promoted);
    }

    /** Restores the unspent portion of temporarily promoted foretell-only mana. */
    public void restoreForetellSpellOnlyMana(ForetellSpellOnlyManaState state) {
        for (ManaColor color : ManaColor.values()) {
            int promotedAmount = state.promoted().getOrDefault(color, 0);
            int spent = Math.max(0, state.regularBefore().getOrDefault(color, 0)
                    + promotedAmount - pool.getOrDefault(color, 0));
            int remaining = Math.max(0, promotedAmount - spent);
            if (remaining > 0) {
                pool.merge(color, -remaining, Integer::sum);
                if (color == ManaColor.COLORLESS) {
                    foretellSpellOnlyColorless += remaining;
                } else {
                    foretellSpellOnlyColored.merge(color, remaining, Integer::sum);
                }
            }
        }
    }

    public record ForetellSpellOnlyManaState(Map<ManaColor, Integer> regularBefore,
                                             Map<ManaColor, Integer> promoted) {
    }

    /** Returns a copy in which only foretell-or-instant/sorcery mana is exposed as instant/sorcery mana. */
    public ManaPool copyForForetellPayment() {
        ManaPool copy = new ManaPool(this);
        copy.instantSorceryOnlyColorless = copy.foretellOrInstantSorceryOnlyColorless
                + copy.foretellSpellOnlyColorless;
        for (ManaColor color : ManaColor.values()) {
            copy.instantSorceryOnlyColored.put(color,
                    copy.foretellOrInstantSorceryOnlyColored.getOrDefault(color, 0)
                            + copy.foretellSpellOnlyColored.getOrDefault(color, 0));
        }
        copy.foretellOrInstantSorceryOnlyColorless = 0;
        copy.foretellOrInstantSorceryOnlyColored.replaceAll((color, amount) -> 0);
        copy.disturbOrInstantSorceryOnlyColorless = 0;
        copy.disturbOrInstantSorceryOnlyColored.replaceAll((color, amount) -> 0);
        copy.foretellSpellOnlyColorless = 0;
        copy.foretellSpellOnlyColored.replaceAll((color, amount) -> 0);
        return copy;
    }

    /** Returns a copy in which only disturb-or-instant/sorcery mana is exposed as instant/sorcery mana. */
    public ManaPool copyForDisturbPayment() {
        ManaPool copy = new ManaPool(this);
        copy.instantSorceryOnlyColorless = copy.disturbOrInstantSorceryOnlyColorless;
        for (ManaColor color : ManaColor.values()) {
            copy.instantSorceryOnlyColored.put(color,
                    copy.disturbOrInstantSorceryOnlyColored.getOrDefault(color, 0));
        }
        copy.disturbOrInstantSorceryOnlyColorless = 0;
        copy.disturbOrInstantSorceryOnlyColored.replaceAll((color, amount) -> 0);
        copy.foretellOrInstantSorceryOnlyColorless = 0;
        copy.foretellOrInstantSorceryOnlyColored.replaceAll((color, amount) -> 0);
        copy.foretellSpellOnlyColorless = 0;
        copy.foretellSpellOnlyColored.replaceAll((color, amount) -> 0);
        return copy;
    }

    /** Temporarily exposes foretell-or-instant/sorcery mana for an actual foretell payment. */
    public ForetellPaymentState beginForetellPayment() {
        ForetellPaymentState state = new ForetellPaymentState(
                instantSorceryOnlyColorless, new EnumMap<>(instantSorceryOnlyColored),
                foretellOrInstantSorceryOnlyColorless, new EnumMap<>(foretellOrInstantSorceryOnlyColored),
                disturbOrInstantSorceryOnlyColorless, new EnumMap<>(disturbOrInstantSorceryOnlyColored),
                foretellSpellOnlyColorless, new EnumMap<>(foretellSpellOnlyColored));
        instantSorceryOnlyColorless = foretellOrInstantSorceryOnlyColorless + foretellSpellOnlyColorless;
        for (ManaColor color : ManaColor.values()) {
            instantSorceryOnlyColored.put(color,
                    foretellOrInstantSorceryOnlyColored.getOrDefault(color, 0)
                            + foretellSpellOnlyColored.getOrDefault(color, 0));
        }
        foretellOrInstantSorceryOnlyColorless = 0;
        foretellOrInstantSorceryOnlyColored.replaceAll((color, amount) -> 0);
        disturbOrInstantSorceryOnlyColorless = 0;
        disturbOrInstantSorceryOnlyColored.replaceAll((color, amount) -> 0);
        foretellSpellOnlyColorless = 0;
        foretellSpellOnlyColored.replaceAll((color, amount) -> 0);
        return state;
    }

    /** Restores ordinary instant/sorcery mana after the foretell payment and preserves leftovers. */
    public void endForetellPayment(ForetellPaymentState state) {
        int remainingColorless = instantSorceryOnlyColorless;
        EnumMap<ManaColor, Integer> remainingColored = new EnumMap<>(instantSorceryOnlyColored);
        instantSorceryOnlyColorless = state.instantSorceryOnlyColorless();
        instantSorceryOnlyColored.putAll(state.instantSorceryOnlyColored());
        foretellOrInstantSorceryOnlyColorless = Math.min(
                state.foretellOrInstantSorceryOnlyColorless(), remainingColorless);
        foretellSpellOnlyColorless = remainingColorless - foretellOrInstantSorceryOnlyColorless;
        for (ManaColor color : ManaColor.values()) {
            int oldRemaining = Math.min(
                    state.foretellOrInstantSorceryOnlyColored().getOrDefault(color, 0),
                    remainingColored.getOrDefault(color, 0));
            foretellOrInstantSorceryOnlyColored.put(color, oldRemaining);
            foretellSpellOnlyColored.put(color,
                    remainingColored.getOrDefault(color, 0) - oldRemaining);
        }
        disturbOrInstantSorceryOnlyColorless = state.disturbOrInstantSorceryOnlyColorless();
        disturbOrInstantSorceryOnlyColored.putAll(state.disturbOrInstantSorceryOnlyColored());
    }

    public record ForetellPaymentState(int instantSorceryOnlyColorless,
                                       Map<ManaColor, Integer> instantSorceryOnlyColored,
                                       int foretellOrInstantSorceryOnlyColorless,
                                       Map<ManaColor, Integer> foretellOrInstantSorceryOnlyColored,
                                       int disturbOrInstantSorceryOnlyColorless,
                                       Map<ManaColor, Integer> disturbOrInstantSorceryOnlyColored,
                                       int foretellSpellOnlyColorless,
                                       Map<ManaColor, Integer> foretellSpellOnlyColored) {
    }

    /** Temporarily exposes disturb-or-instant/sorcery mana for an actual disturb payment. */
    public DisturbPaymentState beginDisturbPayment() {
        DisturbPaymentState state = new DisturbPaymentState(
                instantSorceryOnlyColorless, new EnumMap<>(instantSorceryOnlyColored),
                disturbOrInstantSorceryOnlyColorless, new EnumMap<>(disturbOrInstantSorceryOnlyColored),
                foretellOrInstantSorceryOnlyColorless, new EnumMap<>(foretellOrInstantSorceryOnlyColored),
                foretellSpellOnlyColorless, new EnumMap<>(foretellSpellOnlyColored));
        instantSorceryOnlyColorless = disturbOrInstantSorceryOnlyColorless;
        instantSorceryOnlyColored.putAll(disturbOrInstantSorceryOnlyColored);
        disturbOrInstantSorceryOnlyColorless = 0;
        disturbOrInstantSorceryOnlyColored.replaceAll((color, amount) -> 0);
        foretellOrInstantSorceryOnlyColorless = 0;
        foretellOrInstantSorceryOnlyColored.replaceAll((color, amount) -> 0);
        foretellSpellOnlyColorless = 0;
        foretellSpellOnlyColored.replaceAll((color, amount) -> 0);
        return state;
    }

    /** Restores ordinary instant/sorcery mana after a disturb payment and preserves leftovers. */
    public void endDisturbPayment(DisturbPaymentState state) {
        int remainingColorless = instantSorceryOnlyColorless;
        EnumMap<ManaColor, Integer> remainingColored = new EnumMap<>(instantSorceryOnlyColored);
        instantSorceryOnlyColorless = state.instantSorceryOnlyColorless();
        instantSorceryOnlyColored.putAll(state.instantSorceryOnlyColored());
        disturbOrInstantSorceryOnlyColorless = Math.min(
                state.disturbOrInstantSorceryOnlyColorless(), remainingColorless);
        for (ManaColor color : ManaColor.values()) {
            disturbOrInstantSorceryOnlyColored.put(color, Math.min(
                    state.disturbOrInstantSorceryOnlyColored().getOrDefault(color, 0),
                    remainingColored.getOrDefault(color, 0)));
        }
        foretellOrInstantSorceryOnlyColorless = state.foretellOrInstantSorceryOnlyColorless();
        foretellOrInstantSorceryOnlyColored.putAll(state.foretellOrInstantSorceryOnlyColored());
        foretellSpellOnlyColorless = state.foretellSpellOnlyColorless();
        foretellSpellOnlyColored.putAll(state.foretellSpellOnlyColored());
    }

    public record DisturbPaymentState(int instantSorceryOnlyColorless,
                                      Map<ManaColor, Integer> instantSorceryOnlyColored,
                                      int disturbOrInstantSorceryOnlyColorless,
                                      Map<ManaColor, Integer> disturbOrInstantSorceryOnlyColored,
                                      int foretellOrInstantSorceryOnlyColorless,
                                      Map<ManaColor, Integer> foretellOrInstantSorceryOnlyColored,
                                      int foretellSpellOnlyColorless,
                                      Map<ManaColor, Integer> foretellSpellOnlyColored) {
    }

    public int getXCostOnlyColorless() {
        return xCostOnlyColorless;
    }

    public void addXCostOnlyColorless(int amount) {
        xCostOnlyColorless += amount;
    }

    public void removeXCostOnlyColorless(int amount) {
        xCostOnlyColorless = Math.max(0, xCostOnlyColorless - amount);
    }

    public int getCumulativeUpkeepOnlyColorless() {
        return cumulativeUpkeepOnlyColorless;
    }

    public void addCumulativeUpkeepOnlyColorless(int amount) {
        cumulativeUpkeepOnlyColorless += amount;
    }

    public void removeCumulativeUpkeepOnlyColorless(int amount) {
        cumulativeUpkeepOnlyColorless = Math.max(0, cumulativeUpkeepOnlyColorless - amount);
    }

    public int getCumulativeUpkeepOnlyColored(ManaColor color) {
        return cumulativeUpkeepOnlyColored.getOrDefault(color, 0);
    }

    public int getCumulativeUpkeepOnlyColoredTotal() {
        int total = 0;
        for (int value : cumulativeUpkeepOnlyColored.values()) {
            total += value;
        }
        return total;
    }

    public void addCumulativeUpkeepOnlyColored(ManaColor color, int amount) {
        cumulativeUpkeepOnlyColored.merge(color, amount, Integer::sum);
    }

    public void removeCumulativeUpkeepOnlyColored(ManaColor color, int amount) {
        int current = cumulativeUpkeepOnlyColored.getOrDefault(color, 0);
        cumulativeUpkeepOnlyColored.put(color, Math.max(0, current - amount));
    }

    public int getInstantSorceryOnlyColored(ManaColor color) {
        return instantSorceryOnlyColored.getOrDefault(color, 0)
                + foretellOrInstantSorceryOnlyColored.getOrDefault(color, 0)
                + disturbOrInstantSorceryOnlyColored.getOrDefault(color, 0);
    }

    public int getInstantSorceryOnlyColoredTotal() {
        int total = 0;
        for (ManaColor color : ManaColor.values()) {
            total += getInstantSorceryOnlyColored(color);
        }
        return total;
    }

    public void addInstantSorceryOnlyColored(ManaColor color, int amount) {
        instantSorceryOnlyColored.merge(color, amount, Integer::sum);
    }

    public void removeInstantSorceryOnlyColored(ManaColor color, int amount) {
        int fromDisturb = Math.min(amount, disturbOrInstantSorceryOnlyColored.getOrDefault(color, 0));
        if (fromDisturb > 0) {
            removeDisturbOrInstantSorceryOnlyColored(color, fromDisturb);
            amount -= fromDisturb;
        }
        int fromForetell = Math.min(amount, foretellOrInstantSorceryOnlyColored.getOrDefault(color, 0));
        if (fromForetell > 0) {
            removeForetellOrInstantSorceryOnlyColored(color, fromForetell);
            amount -= fromForetell;
        }
        int current = instantSorceryOnlyColored.getOrDefault(color, 0);
        instantSorceryOnlyColored.put(color, Math.max(0, current - amount));
    }

    public int getForetellOrInstantSorceryOnlyColored(ManaColor color) {
        return foretellOrInstantSorceryOnlyColored.getOrDefault(color, 0);
    }

    public int getForetellOrInstantSorceryOnlyColoredTotal() {
        int total = 0;
        for (int value : foretellOrInstantSorceryOnlyColored.values()) {
            total += value;
        }
        return total;
    }

    public void addForetellOrInstantSorceryOnlyColored(ManaColor color, int amount) {
        foretellOrInstantSorceryOnlyColored.merge(color, amount, Integer::sum);
    }

    public void removeForetellOrInstantSorceryOnlyColored(ManaColor color, int amount) {
        int current = foretellOrInstantSorceryOnlyColored.getOrDefault(color, 0);
        foretellOrInstantSorceryOnlyColored.put(color, Math.max(0, current - amount));
    }

    public int getDisturbOrInstantSorceryOnlyColored(ManaColor color) {
        return disturbOrInstantSorceryOnlyColored.getOrDefault(color, 0);
    }

    public int getDisturbOrInstantSorceryOnlyColoredTotal() {
        int total = 0;
        for (int value : disturbOrInstantSorceryOnlyColored.values()) {
            total += value;
        }
        return total;
    }

    public void addDisturbOrInstantSorceryOnlyColored(ManaColor color, int amount) {
        disturbOrInstantSorceryOnlyColored.merge(color, amount, Integer::sum);
    }

    public void removeDisturbOrInstantSorceryOnlyColored(ManaColor color, int amount) {
        int current = disturbOrInstantSorceryOnlyColored.getOrDefault(color, 0);
        disturbOrInstantSorceryOnlyColored.put(color, Math.max(0, current - amount));
    }

    public void addSubtypeCreatureMana(CardSubtype subtype, ManaColor color, int amount) {
        addSubtypeCreatureMana(subtype, color, amount, false);
    }

    /**
     * Adds subtype-restricted creature-spell mana. When {@code grantsUncounterable} is set, the mana
     * is also recorded in the uncounterable-granting subset (Cavern of Souls), so spending it marks
     * the spell it paid for as uncounterable.
     */
    public void addSubtypeCreatureMana(CardSubtype subtype, ManaColor color, int amount, boolean grantsUncounterable) {
        bucketFor(subtypeCreatureMana, subtype).merge(color, amount, Integer::sum);
        if (grantsUncounterable) {
            bucketFor(uncounterableSubtypeCreatureMana, subtype).merge(color, amount, Integer::sum);
        }
    }

    private EnumMap<ManaColor, Integer> bucketFor(Map<CardSubtype, EnumMap<ManaColor, Integer>> buckets, CardSubtype subtype) {
        return buckets.computeIfAbsent(subtype, k -> {
            EnumMap<ManaColor, Integer> m = new EnumMap<>(ManaColor.class);
            for (ManaColor c : ManaColor.values()) m.put(c, 0);
            return m;
        });
    }

    /**
     * Returns the total mana of the given color available across all matching subtypes.
     */
    public int getSubtypeCreatureManaForColor(Set<CardSubtype> subtypes, ManaColor color) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeCreatureMana.get(subtype);
            if (colorMap != null) {
                total += colorMap.getOrDefault(color, 0);
            }
        }
        return total;
    }

    /**
     * Returns the total mana of all colors available across all matching subtypes.
     */
    public int getSubtypeCreatureManaTotal(Set<CardSubtype> subtypes) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeCreatureMana.get(subtype);
            if (colorMap != null) {
                for (int v : colorMap.values()) {
                    total += v;
                }
            }
        }
        return total;
    }

    /**
     * Removes mana of the given color from subtype creature mana pools matching any of the given subtypes.
     * Distributes the removal across matching subtypes.
     */
    public void removeSubtypeCreatureMana(Set<CardSubtype> subtypes, ManaColor color, int amount) {
        int remaining = amount;
        for (CardSubtype subtype : subtypes) {
            if (remaining <= 0) break;
            EnumMap<ManaColor, Integer> colorMap = subtypeCreatureMana.get(subtype);
            if (colorMap != null) {
                int available = colorMap.getOrDefault(color, 0);
                int toRemove = Math.min(remaining, available);
                colorMap.put(color, available - toRemove);
                remaining -= toRemove;
                consumeUncounterableGranting(subtype, color, toRemove);
            }
        }
    }

    /**
     * Deducts up to {@code spent} from the uncounterable-granting subset of the given subtype/color
     * bucket, flagging the payment when any of it was uncounterable-granting mana. Deducting the
     * rider-carrying mana first stands in for the caster's spend choice, which always favours it.
     */
    private void consumeUncounterableGranting(CardSubtype subtype, ManaColor color, int spent) {
        if (spent <= 0) {
            return;
        }
        EnumMap<ManaColor, Integer> riderMap = uncounterableSubtypeCreatureMana.get(subtype);
        if (riderMap == null) {
            return;
        }
        int available = riderMap.getOrDefault(color, 0);
        int consumed = Math.min(spent, available);
        if (consumed > 0) {
            riderMap.put(color, available - consumed);
            spentUncounterableGrantingMana = true;
        }
    }

    /**
     * Returns whether uncounterable-granting mana was spent since the last call, resetting the flag.
     */
    public boolean consumeSpentUncounterableGrantingMana() {
        boolean spent = spentUncounterableGrantingMana;
        spentUncounterableGrantingMana = false;
        return spent;
    }

    public void addSubtypeSpellOrAbilityMana(CardSubtype subtype, ManaColor color, int amount) {
        subtypeSpellOrAbilityMana.computeIfAbsent(subtype, k -> {
            EnumMap<ManaColor, Integer> m = new EnumMap<>(ManaColor.class);
            for (ManaColor c : ManaColor.values()) m.put(c, 0);
            return m;
        }).merge(color, amount, Integer::sum);
    }

    public void addSubtypeSpellOnlyMana(Set<CardSubtype> subtypes, ManaColor color, int amount) {
        subtypeSpellOnlyMana.computeIfAbsent(Set.copyOf(subtypes), k -> {
            EnumMap<ManaColor, Integer> m = new EnumMap<>(ManaColor.class);
            for (ManaColor c : ManaColor.values()) m.put(c, 0);
            return m;
        }).merge(color, amount, Integer::sum);
    }

    public int getSubtypeSpellOnlyManaForColor(Set<CardSubtype> subtypes, ManaColor color) {
        int total = 0;
        for (Map.Entry<Set<CardSubtype>, EnumMap<ManaColor, Integer>> entry : subtypeSpellOnlyMana.entrySet()) {
            if (entry.getKey().stream().anyMatch(subtypes::contains)) {
                total += entry.getValue().getOrDefault(color, 0);
            }
        }
        return total;
    }

    public int getSubtypeSpellOnlyManaTotal(Set<CardSubtype> subtypes) {
        int total = 0;
        for (Map.Entry<Set<CardSubtype>, EnumMap<ManaColor, Integer>> entry : subtypeSpellOnlyMana.entrySet()) {
            if (entry.getKey().stream().anyMatch(subtypes::contains)) {
                total += entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            }
        }
        return total;
    }

    public void removeSubtypeSpellOnlyMana(Set<CardSubtype> subtypes, ManaColor color, int amount) {
        int remaining = amount;
        for (Map.Entry<Set<CardSubtype>, EnumMap<ManaColor, Integer>> entry : subtypeSpellOnlyMana.entrySet()) {
            if (remaining <= 0) {
                break;
            }
            if (!entry.getKey().stream().anyMatch(subtypes::contains)) {
                continue;
            }
            EnumMap<ManaColor, Integer> colorMap = entry.getValue();
            int available = colorMap.getOrDefault(color, 0);
            int removed = Math.min(remaining, available);
            colorMap.put(color, available - removed);
            remaining -= removed;
        }
    }

    /** Adds mana spendable for any of the given subtypes, sharing one bucket for the party types. */
    public void addSubtypeSpellOrAbilityMana(Set<CardSubtype> subtypes, ManaColor color, int amount) {
        if (containsPartySubtype(subtypes)) {
            partySpellOrAbilityMana.merge(color, amount, Integer::sum);
            return;
        }
        for (CardSubtype subtype : subtypes) {
            addSubtypeSpellOrAbilityMana(subtype, color, amount);
        }
    }

    /** Total spell-or-ability mana of the given color available across all matching subtypes. */
    public int getSubtypeSpellOrAbilityManaForColor(Set<CardSubtype> subtypes, ManaColor color) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeSpellOrAbilityMana.get(subtype);
            if (colorMap != null) {
                total += colorMap.getOrDefault(color, 0);
            }
            if (color == ManaColor.COLORLESS) {
                total += getColorlessSubtypeSpellOrAbilityMana(subtype);
            }
        }
        if (containsPartySubtype(subtypes)) {
            total += partySpellOrAbilityMana.getOrDefault(color, 0);
        }
        return total;
    }

    /** Total spell-or-ability mana of all colors available across all matching subtypes. */
    public int getSubtypeSpellOrAbilityManaTotal(Set<CardSubtype> subtypes) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeSpellOrAbilityMana.get(subtype);
            if (colorMap != null) {
                for (int v : colorMap.values()) {
                    total += v;
                }
            }
            total += getColorlessSubtypeSpellOrAbilityMana(subtype);
        }
        if (containsPartySubtype(subtypes)) {
            for (int v : partySpellOrAbilityMana.values()) {
                total += v;
            }
        }
        return total;
    }

    /**
     * Removes mana of the given color from spell-or-ability mana pools matching any of the given
     * subtypes. Distributes the removal across matching subtypes.
     */
    public void removeSubtypeSpellOrAbilityMana(Set<CardSubtype> subtypes, ManaColor color, int amount) {
        int remaining = amount;
        for (CardSubtype subtype : subtypes) {
            if (remaining <= 0) break;
            EnumMap<ManaColor, Integer> colorMap = subtypeSpellOrAbilityMana.get(subtype);
            if (colorMap != null) {
                int available = colorMap.getOrDefault(color, 0);
                int toRemove = Math.min(remaining, available);
                colorMap.put(color, available - toRemove);
                remaining -= toRemove;
            }
            if (remaining > 0 && color == ManaColor.COLORLESS) {
                int available = getColorlessSubtypeSpellOrAbilityMana(subtype);
                int toRemove = Math.min(remaining, available);
                removeColorlessSubtypeSpellOrAbilityMana(subtype, toRemove);
                remaining -= toRemove;
            }
        }
        if (remaining > 0 && containsPartySubtype(subtypes)) {
            int available = partySpellOrAbilityMana.getOrDefault(color, 0);
            partySpellOrAbilityMana.put(color, Math.max(0, available - remaining));
        }
    }

    private static boolean containsPartySubtype(Set<CardSubtype> subtypes) {
        return subtypes != null && (subtypes.contains(CardSubtype.CLERIC)
                || subtypes.contains(CardSubtype.ROGUE)
                || subtypes.contains(CardSubtype.WARRIOR)
                || subtypes.contains(CardSubtype.WIZARD));
    }

    public void addSubtypeCreatureSourceSpellOrAbilityMana(CardSubtype subtype, ManaColor color, int amount) {
        subtypeCreatureSourceSpellOrAbilityMana.computeIfAbsent(subtype, k -> {
            EnumMap<ManaColor, Integer> m = new EnumMap<>(ManaColor.class);
            for (ManaColor c : ManaColor.values()) m.put(c, 0);
            return m;
        }).merge(color, amount, Integer::sum);
    }

    public int getSubtypeCreatureSourceSpellOrAbilityManaForColor(Set<CardSubtype> subtypes, ManaColor color) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeCreatureSourceSpellOrAbilityMana.get(subtype);
            if (colorMap != null) {
                total += colorMap.getOrDefault(color, 0);
            }
        }
        return total;
    }

    public int getSubtypeCreatureSourceSpellOrAbilityManaTotal(Set<CardSubtype> subtypes) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeCreatureSourceSpellOrAbilityMana.get(subtype);
            if (colorMap != null) {
                for (int value : colorMap.values()) {
                    total += value;
                }
            }
        }
        return total;
    }

    public void removeSubtypeCreatureSourceSpellOrAbilityMana(Set<CardSubtype> subtypes,
                                                               ManaColor color, int amount) {
        int remaining = amount;
        for (CardSubtype subtype : subtypes) {
            if (remaining <= 0) break;
            EnumMap<ManaColor, Integer> colorMap = subtypeCreatureSourceSpellOrAbilityMana.get(subtype);
            if (colorMap != null) {
                int available = colorMap.getOrDefault(color, 0);
                int toRemove = Math.min(remaining, available);
                colorMap.put(color, available - toRemove);
                remaining -= toRemove;
            }
        }
    }

    public void addSubtypeOrPlaneswalkerSpellMana(ManaRestriction.SubtypeOrPlaneswalkerSpells restriction,
                                                   ManaColor color, int amount) {
        subtypeOrPlaneswalkerSpellMana.computeIfAbsent(restriction, ignored -> {
            EnumMap<ManaColor, Integer> m = new EnumMap<>(ManaColor.class);
            for (ManaColor c : ManaColor.values()) m.put(c, 0);
            return m;
        }).merge(color, amount, Integer::sum);
    }

    /** Total mana of the given color available from matching subtype-or-planeswalker restrictions. */
    public int getSubtypeOrPlaneswalkerSpellManaForColor(
            Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> restrictions, ManaColor color) {
        int total = 0;
        for (ManaRestriction.SubtypeOrPlaneswalkerSpells restriction : restrictions) {
            EnumMap<ManaColor, Integer> colorMap = subtypeOrPlaneswalkerSpellMana.get(restriction);
            if (colorMap != null) {
                total += colorMap.getOrDefault(color, 0);
            }
        }
        return total;
    }

    /** Total mana of all colors available from matching subtype-or-planeswalker restrictions. */
    public int getSubtypeOrPlaneswalkerSpellManaTotal(
            Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> restrictions) {
        int total = 0;
        for (ManaRestriction.SubtypeOrPlaneswalkerSpells restriction : restrictions) {
            EnumMap<ManaColor, Integer> colorMap = subtypeOrPlaneswalkerSpellMana.get(restriction);
            if (colorMap != null) {
                for (int value : colorMap.values()) {
                    total += value;
                }
            }
        }
        return total;
    }

    /** Removes mana of the given color from matching subtype-or-planeswalker restrictions. */
    public void removeSubtypeOrPlaneswalkerSpellMana(
            Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> restrictions, ManaColor color, int amount) {
        int remaining = amount;
        for (ManaRestriction.SubtypeOrPlaneswalkerSpells restriction : restrictions) {
            if (remaining <= 0) break;
            EnumMap<ManaColor, Integer> colorMap = subtypeOrPlaneswalkerSpellMana.get(restriction);
            if (colorMap != null) {
                int available = colorMap.getOrDefault(color, 0);
                int toRemove = Math.min(remaining, available);
                colorMap.put(color, available - toRemove);
                remaining -= toRemove;
            }
        }
    }

    /** Adds creature-spell-only mana of the given color (Ancient Ziggurat). */
    public void addCreatureSpellOnlyMana(ManaColor color, int amount) {
        creatureSpellOnlyMana.merge(color, amount, Integer::sum);
    }

    public int getCreatureSpellOnlyMana(ManaColor color) {
        return creatureSpellOnlyMana.getOrDefault(color, 0);
    }

    public int getCreatureSpellOnlyManaTotal() {
        int total = 0;
        for (int value : creatureSpellOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    public void removeCreatureSpellOnlyMana(ManaColor color, int amount) {
        int current = creatureSpellOnlyMana.getOrDefault(color, 0);
        creatureSpellOnlyMana.put(color, Math.max(0, current - amount));
    }

    public void addCreatureOrEnchantmentSpellOnlyMana(ManaColor color, int amount) {
        creatureOrEnchantmentSpellOnlyMana.merge(color, amount, Integer::sum);
    }

    public int getCreatureOrEnchantmentSpellOnlyMana(ManaColor color) {
        return creatureOrEnchantmentSpellOnlyMana.getOrDefault(color, 0);
    }

    public int getCreatureOrEnchantmentSpellOnlyManaTotal() {
        int total = 0;
        for (int value : creatureOrEnchantmentSpellOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    public void removeCreatureOrEnchantmentSpellOnlyMana(ManaColor color, int amount) {
        int current = creatureOrEnchantmentSpellOnlyMana.getOrDefault(color, 0);
        creatureOrEnchantmentSpellOnlyMana.put(color, Math.max(0, current - amount));
    }

    /** Temporarily exposes this restricted mana to the ordinary spell-payment algorithm. */
    public CreatureOrEnchantmentSpellManaState promoteCreatureOrEnchantmentSpellOnlyMana() {
        EnumMap<ManaColor, Integer> regularBefore = new EnumMap<>(ManaColor.class);
        EnumMap<ManaColor, Integer> promoted = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            regularBefore.put(color, pool.getOrDefault(color, 0));
            int amount = getCreatureOrEnchantmentSpellOnlyMana(color);
            promoted.put(color, amount);
            if (amount > 0) {
                pool.merge(color, amount, Integer::sum);
                creatureOrEnchantmentSpellOnlyMana.put(color, 0);
            }
        }
        return new CreatureOrEnchantmentSpellManaState(regularBefore, promoted);
    }

    public void addSubtypeOrLegendaryCreatureMana(CardSubtype subtype, ManaColor color, int amount) {
        bucketFor(subtypeOrLegendaryCreatureMana, subtype).merge(color, amount, Integer::sum);
    }

    public int getSubtypeOrLegendaryCreatureManaForColor(Set<CardSubtype> subtypes, ManaColor color) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeOrLegendaryCreatureMana.get(subtype);
            if (colorMap != null) {
                total += colorMap.getOrDefault(color, 0);
            }
        }
        return total;
    }

    public int getSubtypeOrLegendaryCreatureManaTotal(Set<CardSubtype> subtypes) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeOrLegendaryCreatureMana.get(subtype);
            if (colorMap != null) {
                for (int value : colorMap.values()) {
                    total += value;
                }
            }
        }
        return total;
    }

    public void removeSubtypeOrLegendaryCreatureMana(Set<CardSubtype> subtypes, ManaColor color, int amount) {
        int remaining = amount;
        for (CardSubtype subtype : subtypes) {
            if (remaining <= 0) break;
            EnumMap<ManaColor, Integer> colorMap = subtypeOrLegendaryCreatureMana.get(subtype);
            if (colorMap != null) {
                int available = colorMap.getOrDefault(color, 0);
                int toRemove = Math.min(remaining, available);
                colorMap.put(color, available - toRemove);
                remaining -= toRemove;
            }
        }
    }

    /** Temporarily exposes matching subtype-or-legendary creature mana to spell payment. */
    public SubtypeOrLegendaryCreatureManaState promoteSubtypeOrLegendaryCreatureMana(
            Set<CardSubtype> subtypes) {
        EnumMap<ManaColor, Integer> regularBefore = new EnumMap<>(ManaColor.class);
        EnumMap<CardSubtype, EnumMap<ManaColor, Integer>> promoted = new EnumMap<>(CardSubtype.class);
        for (ManaColor color : ManaColor.values()) {
            regularBefore.put(color, get(color));
        }
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> bucket = subtypeOrLegendaryCreatureMana.get(subtype);
            if (bucket == null) {
                continue;
            }
            EnumMap<ManaColor, Integer> amounts = new EnumMap<>(ManaColor.class);
            for (ManaColor color : ManaColor.values()) {
                int amount = bucket.getOrDefault(color, 0);
                amounts.put(color, amount);
                if (amount > 0) {
                    pool.merge(color, amount, Integer::sum);
                    bucket.put(color, 0);
                }
            }
            promoted.put(subtype, amounts);
        }
        return new SubtypeOrLegendaryCreatureManaState(regularBefore, promoted);
    }

    /** Restores the unspent portion of temporarily promoted subtype-or-legendary mana. */
    public void restorePromotedSubtypeOrLegendaryCreatureMana(
            SubtypeOrLegendaryCreatureManaState state) {
        for (ManaColor color : ManaColor.values()) {
            int promotedTotal = state.promoted().values().stream()
                    .mapToInt(amounts -> amounts.getOrDefault(color, 0))
                    .sum();
            int spent = Math.max(0, state.regularBefore().getOrDefault(color, 0)
                    + promotedTotal - get(color));
            int remaining = Math.max(0, promotedTotal - spent);
            if (remaining == 0) {
                continue;
            }
            pool.merge(color, -remaining, Integer::sum);
            for (Map.Entry<CardSubtype, EnumMap<ManaColor, Integer>> entry : state.promoted().entrySet()) {
                if (remaining == 0) {
                    break;
                }
                int restore = Math.min(remaining, entry.getValue().getOrDefault(color, 0));
                if (restore > 0) {
                    bucketFor(subtypeOrLegendaryCreatureMana, entry.getKey()).merge(color, restore, Integer::sum);
                    remaining -= restore;
                }
            }
        }
    }

    public record SubtypeOrLegendaryCreatureManaState(
            Map<ManaColor, Integer> regularBefore,
            Map<CardSubtype, EnumMap<ManaColor, Integer>> promoted) {
    }

    /** Restores the unspent portion of temporarily promoted restricted mana. */
    public void restorePromotedCreatureOrEnchantmentSpellOnlyMana(
            CreatureOrEnchantmentSpellManaState state) {
        for (ManaColor color : ManaColor.values()) {
            int promoted = state.promoted().getOrDefault(color, 0);
            int spent = Math.max(0, state.regularBefore().getOrDefault(color, 0)
                    + promoted - pool.getOrDefault(color, 0));
            int remaining = Math.max(0, promoted - spent);
            if (remaining > 0) {
                pool.merge(color, -remaining, Integer::sum);
                creatureOrEnchantmentSpellOnlyMana.merge(color, remaining, Integer::sum);
            }
        }
    }

    public record CreatureOrEnchantmentSpellManaState(Map<ManaColor, Integer> regularBefore,
                                                        Map<ManaColor, Integer> promoted) {
    }

    /** Adds mana spendable only to cast creature spells or activate abilities of creature sources (Gwenna). */
    public void addCreatureSpellOrAbilityMana(ManaColor color, int amount) {
        creatureSpellOrAbilityMana.merge(color, amount, Integer::sum);
    }

    public int getCreatureSpellOrAbilityMana(ManaColor color) {
        return creatureSpellOrAbilityMana.getOrDefault(color, 0);
    }

    public int getCreatureSpellOrAbilityManaTotal() {
        int total = 0;
        for (int value : creatureSpellOrAbilityMana.values()) {
            total += value;
        }
        return total;
    }

    public void removeCreatureSpellOrAbilityMana(ManaColor color, int amount) {
        int current = creatureSpellOrAbilityMana.getOrDefault(color, 0);
        creatureSpellOrAbilityMana.put(color, Math.max(0, current - amount));
    }

    /** Temporarily exposes creature-source-only mana to the ordinary payment algorithm. */
    public EnumMap<ManaColor, Integer> promoteCreatureSpellOrAbilityMana() {
        EnumMap<ManaColor, Integer> promoted = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            int amount = getCreatureSpellOrAbilityMana(color);
            promoted.put(color, amount);
            if (amount > 0) {
                pool.merge(color, amount, Integer::sum);
                creatureSpellOrAbilityMana.put(color, 0);
            }
        }
        return promoted;
    }

    /** Restores the portion of temporarily promoted mana that the payment did not spend. */
    public void restorePromotedCreatureSpellOrAbilityMana(EnumMap<ManaColor, Integer> promoted,
                                                           EnumMap<ManaColor, Integer> regularBefore) {
        for (ManaColor color : ManaColor.values()) {
            int promotedAmount = promoted.getOrDefault(color, 0);
            int spent = Math.max(0, regularBefore.getOrDefault(color, 0) + promotedAmount - get(color));
            int remaining = Math.max(0, promotedAmount - spent);
            if (remaining > 0) {
                pool.put(color, get(color) - remaining);
                creatureSpellOrAbilityMana.merge(color, remaining, Integer::sum);
            }
        }
    }

    /** Adds mana spendable only to cast spells with mana value 4 or greater (Ashling, Rimebound). */
    public void addManaValueAtLeastFourOnlyMana(ManaColor color, int amount) {
        manaValueAtLeastFourOnlyMana.merge(color, amount, Integer::sum);
    }

    public int getManaValueAtLeastFourOnlyMana(ManaColor color) {
        return manaValueAtLeastFourOnlyMana.getOrDefault(color, 0);
    }

    public int getManaValueAtLeastFourOnlyManaTotal() {
        int total = 0;
        for (int value : manaValueAtLeastFourOnlyMana.values()) {
            total += value;
        }
        return total;
    }

    public void removeManaValueAtLeastFourOnlyMana(ManaColor color, int amount) {
        int current = manaValueAtLeastFourOnlyMana.getOrDefault(color, 0);
        manaValueAtLeastFourOnlyMana.put(color, Math.max(0, current - amount));
    }

    /**
     * Adds mana that persists through step/phase transitions until end of turn.
     * The mana is added to both the regular pool and the persistent tracker.
     */
    public void addPersistentMana(ManaColor color, int amount) {
        pool.merge(color, amount, Integer::sum);
        persistentMana.merge(color, amount, Integer::sum);
    }

    /**
     * Changes the mana that would drain at the next step or phase boundary to colorless mana.
     * Persistent mana remains in its original color until it would actually drain.
     */
    public void convertNonPersistentManaToColorless() {
        for (ManaColor color : ManaColor.COLORS) {
            int current = pool.getOrDefault(color, 0);
            int persistent = persistentMana.getOrDefault(color, 0);
            int amount = Math.max(0, current - persistent);
            if (amount == 0) {
                continue;
            }

            pool.put(color, current - amount);
            pool.merge(ManaColor.COLORLESS, amount, Integer::sum);
            moveTaggedManaToColorless(snowMana, color, amount);
            moveTaggedManaToColorless(creatureMana, color, amount);
            moveTaggedManaToColorless(spellOnlyMana, color, amount);
            moveTaggedManaToColorless(promotedAbilityOnlyMana, color, amount);
            moveTaggedManaToColorless(hasteGrantingMana, color, amount);
            moveTaggedManaToColorless(uncounterableGrantingMana, color, amount);
            moveTaggedManaToColorless(additionalCounterGrantingMana, color, amount);
            moveTaggedManaToColorless(riotGrantingMana, color, amount);
        }

        artifactOnlyColorless += moveColoredManaToColorless(artifactOnlyMana);
        pool.merge(ManaColor.COLORLESS, restrictedRed + kickedOnlyGreen, Integer::sum);
        restrictedRed = 0;
        kickedOnlyGreen = 0;
        instantSorceryOnlyColorless += moveColoredManaToColorless(instantSorceryOnlyColored);
        foretellOrInstantSorceryOnlyColorless += moveColoredManaToColorless(foretellOrInstantSorceryOnlyColored);
        disturbOrInstantSorceryOnlyColorless += moveColoredManaToColorless(disturbOrInstantSorceryOnlyColored);
        foretellSpellOnlyColorless += moveColoredManaToColorless(foretellSpellOnlyColored);
        cumulativeUpkeepOnlyColorless += moveColoredManaToColorless(cumulativeUpkeepOnlyColored);
        moveColoredManaToColorless(flashbackOnlyMana);
        moveColoredManaToColorless(graveyardOnlyMana);
        moveColoredManaToColorless(abilityOnlyMana);
        moveColoredManaToColorlessBuckets(subtypeCreatureMana);
        moveColoredManaToColorlessBuckets(subtypeOrLegendaryCreatureMana);
        moveColoredManaToColorlessBuckets(uncounterableSubtypeCreatureMana);
        moveColoredManaToColorlessBuckets(subtypeSpellOrAbilityMana);
        moveColoredManaToColorlessBuckets(subtypeSpellOnlyMana);
        moveColoredManaToColorlessBuckets(subtypeCreatureSourceSpellOrAbilityMana);
        moveColoredManaToColorlessBuckets(subtypeOrPlaneswalkerSpellMana);
        moveColoredManaToColorless(partySpellOrAbilityMana);
        moveColoredManaToColorless(creatureSpellOnlyMana);
        moveColoredManaToColorless(creatureOrEnchantmentSpellOnlyMana);
        moveColoredManaToColorless(manaValueAtLeastFourOnlyMana);
        for (Map.Entry<UUID, EnumMap<ManaColor, Integer>> entry : exiledCardOnlyMana.entrySet()) {
            moveColoredManaToColorless(entry.getValue());
        }
        moveColoredManaToColorless(exiledSpellOnlyMana);
    }

    /** Changes all mana that would drain at the next boundary to the requested color. */
    public void convertNonPersistentManaTo(ManaColor replacementColor) {
        if (replacementColor == ManaColor.COLORLESS) {
            convertNonPersistentManaToColorless();
            return;
        }

        for (ManaColor color : ManaColor.values()) {
            if (color == replacementColor) {
                continue;
            }
            int current = pool.getOrDefault(color, 0);
            int persistent = persistentMana.getOrDefault(color, 0);
            int amount = Math.max(0, current - persistent);
            if (amount == 0) {
                continue;
            }
            pool.put(color, current - amount);
            pool.merge(replacementColor, amount, Integer::sum);
            moveTaggedMana(snowMana, color, replacementColor, amount);
            moveTaggedMana(creatureMana, color, replacementColor, amount);
            moveTaggedMana(spellOnlyMana, color, replacementColor, amount);
            moveTaggedMana(promotedAbilityOnlyMana, color, replacementColor, amount);
            moveTaggedMana(hasteGrantingMana, color, replacementColor, amount);
            moveTaggedMana(uncounterableGrantingMana, color, replacementColor, amount);
            moveTaggedMana(additionalCounterGrantingMana, color, replacementColor, amount);
            moveTaggedMana(riotGrantingMana, color, replacementColor, amount);
        }

        moveManaToPool(replacementColor, artifactOnlyColorless);
        artifactOnlyColorless = 0;
        moveManaToPool(replacementColor, artifactAbilityOnlyColorless);
        artifactAbilityOnlyColorless = 0;
        moveManaToPool(replacementColor, powerstoneOnlyColorless);
        powerstoneOnlyColorless = 0;
        moveManaToPool(replacementColor, myrOnlyColorless);
        myrOnlyColorless = 0;
        moveManaToPool(replacementColor, legendarySpellOnlyColorless);
        legendarySpellOnlyColorless = 0;
        moveManaToPool(replacementColor, instantSorceryOnlyColorless);
        instantSorceryOnlyColorless = 0;
        moveManaToPool(replacementColor, foretellOrInstantSorceryOnlyColorless);
        foretellOrInstantSorceryOnlyColorless = 0;
        moveManaToPool(replacementColor, foretellSpellOnlyColorless);
        foretellSpellOnlyColorless = 0;
        moveManaToPool(replacementColor, xCostOnlyColorless);
        xCostOnlyColorless = 0;
        moveManaToPool(replacementColor, cumulativeUpkeepOnlyColorless);
        cumulativeUpkeepOnlyColorless = 0;
        moveManaToPool(replacementColor, restrictedRed);
        restrictedRed = 0;
        moveManaToPool(replacementColor, kickedOnlyGreen);
        kickedOnlyGreen = 0;
        int colorlessSubtypeMana = colorlessSubtypeSpellOrAbilityMana.values().stream()
                .mapToInt(Integer::intValue).sum();
        moveManaToPool(replacementColor, colorlessSubtypeMana);
        colorlessSubtypeSpellOrAbilityMana.clear();

        moveManaTo(replacementColor, artifactOnlyMana);
        moveManaTo(replacementColor, artifactSpellOrAbilityOnlyMana);
        moveManaTo(replacementColor, promotedArtifactSpellOrAbilityOnlyMana);
        moveManaTo(replacementColor, instantSorceryOnlyColored);
        moveManaTo(replacementColor, foretellOrInstantSorceryOnlyColored);
        moveManaTo(replacementColor, foretellSpellOnlyColored);
        moveManaTo(replacementColor, cumulativeUpkeepOnlyColored);
        moveManaTo(replacementColor, flashbackOnlyMana);
        moveManaTo(replacementColor, abilityOnlyMana);
        moveManaToBuckets(replacementColor, subtypeCreatureMana);
        moveManaToBuckets(replacementColor, subtypeOrLegendaryCreatureMana);
        moveManaToBuckets(replacementColor, uncounterableSubtypeCreatureMana);
        moveManaToBuckets(replacementColor, subtypeSpellOrAbilityMana);
        moveManaToBuckets(replacementColor, subtypeSpellOnlyMana);
        moveManaToBuckets(replacementColor, subtypeCreatureSourceSpellOrAbilityMana);
        moveManaToBuckets(replacementColor, subtypeOrPlaneswalkerSpellMana);
        moveManaTo(replacementColor, partySpellOrAbilityMana);
        moveManaTo(replacementColor, creatureSpellOnlyMana);
        moveManaTo(replacementColor, creatureOrEnchantmentSpellOnlyMana);
        moveManaTo(replacementColor, creatureSpellOrAbilityMana);
        moveManaTo(replacementColor, manaValueAtLeastFourOnlyMana);
        for (EnumMap<ManaColor, Integer> bucket : exiledCardOnlyMana.values()) {
            moveManaTo(replacementColor, bucket);
        }
    }

    private void moveManaToPool(ManaColor replacementColor, int amount) {
        if (amount > 0) {
            pool.merge(replacementColor, amount, Integer::sum);
        }
    }

    private static void moveTaggedMana(EnumMap<ManaColor, Integer> bucket,
                                       ManaColor from, ManaColor to, int amount) {
        int tagged = Math.min(amount, bucket.getOrDefault(from, 0));
        if (tagged > 0) {
            bucket.put(from, bucket.get(from) - tagged);
            bucket.merge(to, tagged, Integer::sum);
        }
    }

    private static void moveManaTo(ManaColor replacementColor, Map<ManaColor, Integer> bucket) {
        for (ManaColor color : ManaColor.values()) {
            if (color == replacementColor) {
                continue;
            }
            int amount = bucket.getOrDefault(color, 0);
            if (amount > 0) {
                bucket.put(color, 0);
                bucket.merge(replacementColor, amount, Integer::sum);
            }
        }
    }

    private static void moveManaToBuckets(ManaColor replacementColor,
                                          Map<?, EnumMap<ManaColor, Integer>> buckets) {
        for (EnumMap<ManaColor, Integer> bucket : buckets.values()) {
            moveManaTo(replacementColor, bucket);
        }
    }

    private static void moveTaggedManaToColorless(EnumMap<ManaColor, Integer> tags,
                                                  ManaColor color, int amount) {
        int tagged = Math.min(amount, tags.getOrDefault(color, 0));
        if (tagged > 0) {
            tags.put(color, tags.get(color) - tagged);
            tags.merge(ManaColor.COLORLESS, tagged, Integer::sum);
        }
    }

    private static int moveColoredManaToColorless(Map<ManaColor, Integer> bucket) {
        int total = 0;
        for (ManaColor color : ManaColor.COLORS) {
            int amount = bucket.getOrDefault(color, 0);
            if (amount > 0) {
                bucket.put(color, 0);
                bucket.merge(ManaColor.COLORLESS, amount, Integer::sum);
                total += amount;
            }
        }
        return total;
    }

    private static void moveColoredManaToColorlessBuckets(Map<?, EnumMap<ManaColor, Integer>> buckets) {
        for (EnumMap<ManaColor, Integer> bucket : buckets.values()) {
            moveColoredManaToColorless(bucket);
        }
    }

    /**
     * Drains all non-persistent mana. For each color, the pool is reduced to
     * at most the persistent amount. Persistent mana survives step/phase transitions.
     *
     * @return whether any mana was drained
     */
    public boolean drainNonPersistent() {
        return drainNonPersistent(Set.of());
    }

    /**
     * Drains all non-persistent mana except mana of the given colors.
     *
     * @param protectedColors colors that remain in every mana bucket
     * @return whether any mana was drained
     */
    public boolean drainNonPersistent(Set<ManaColor> protectedColors) {
        int totalBefore = getTotalAllMana();
        for (ManaColor color : ManaColor.values()) {
            int persistent = persistentMana.getOrDefault(color, 0);
            int current = pool.getOrDefault(color, 0);
            if (!protectedColors.contains(color)) {
                // Keep the lesser of current pool and persistent amount
                pool.put(color, Math.min(current, persistent));
            }
            // Clamp persistent to not exceed what's in the pool
            persistentMana.put(color, Math.min(current, persistent));
        }

        clampColorTag(creatureMana, protectedColors);
        clampColorTag(snowMana, protectedColors);
        clampColorTag(spellOnlyMana, protectedColors);
        clampColorTag(hasteGrantingMana, protectedColors);
        clampColorTag(uncounterableGrantingMana, protectedColors);
        clampColorTag(additionalCounterGrantingMana, protectedColors);
        clampColorTag(riotGrantingMana, protectedColors);
        drainColorBucket(abilityOnlyMana, protectedColors);
        drainColorBucket(promotedAbilityOnlyMana, protectedColors);
        drainColorBucket(instantSorceryOnlyColored, protectedColors);
        drainColorBucket(foretellOrInstantSorceryOnlyColored, protectedColors);
        drainColorBucket(disturbOrInstantSorceryOnlyColored, protectedColors);
        drainColorBucket(foretellSpellOnlyColored, protectedColors);
        drainColorBucket(cumulativeUpkeepOnlyColored, protectedColors);
        drainColorBucket(flashbackOnlyMana, protectedColors);
        drainColorBucket(graveyardOnlyMana, protectedColors);
        drainColorBucket(artifactOnlyMana, protectedColors);
        drainColorBucket(artifactSpellOrAbilityOnlyMana, protectedColors);
        drainColorBucket(promotedArtifactSpellOrAbilityOnlyMana, protectedColors);
        drainColorBucket(partySpellOrAbilityMana, protectedColors);
        drainColorBucket(creatureSpellOnlyMana, protectedColors);
        drainColorBucket(creatureSpellOrAbilityMana, protectedColors);
        drainColorBucket(manaValueAtLeastFourOnlyMana, protectedColors);

        drainColorMap(subtypeCreatureMana, protectedColors);
        drainColorMap(uncounterableSubtypeCreatureMana, protectedColors);
        drainColorMap(subtypeSpellOrAbilityMana, protectedColors);
        drainColorMap(subtypeSpellOnlyMana, protectedColors);
        drainColorMap(subtypeCreatureSourceSpellOrAbilityMana, protectedColors);
        drainColorMap(subtypeOrPlaneswalkerSpellMana, protectedColors);
        drainColorMap(exiledCardOnlyMana, protectedColors);
        drainColorBucket(exiledSpellOnlyMana, protectedColors);

        if (!protectedColors.contains(ManaColor.COLORLESS)) {
            artifactOnlyColorless = 0;
            artifactAbilityOnlyColorless = 0;
            powerstoneOnlyColorless = 0;
            myrOnlyColorless = 0;
            colorlessSubtypeSpellOrAbilityMana.clear();
            legendarySpellOnlyColorless = 0;
            instantSorceryOnlyColorless = 0;
            foretellOrInstantSorceryOnlyColorless = 0;
            disturbOrInstantSorceryOnlyColorless = 0;
            foretellSpellOnlyColorless = 0;
            xCostOnlyColorless = 0;
            cumulativeUpkeepOnlyColorless = 0;
        }
        if (!protectedColors.contains(ManaColor.RED)) {
            restrictedRed = 0;
        }
        if (!protectedColors.contains(ManaColor.GREEN)) {
            kickedOnlyGreen = 0;
        }

        spentUncounterableGrantingMana = false;
        return totalBefore != getTotalAllMana();
    }

    private static void drainColorBucket(EnumMap<ManaColor, Integer> bucket,
                                          Set<ManaColor> protectedColors) {
        for (ManaColor color : ManaColor.values()) {
            if (!protectedColors.contains(color)) {
                bucket.put(color, 0);
            }
        }
    }

    private void clampColorTag(EnumMap<ManaColor, Integer> bucket,
                               Set<ManaColor> protectedColors) {
        for (ManaColor color : ManaColor.values()) {
            if (!protectedColors.contains(color)) {
                bucket.put(color, Math.min(bucket.getOrDefault(color, 0), pool.getOrDefault(color, 0)));
            }
        }
    }

    private static void drainColorMap(Map<?, EnumMap<ManaColor, Integer>> buckets,
                                       Set<ManaColor> protectedColors) {
        for (EnumMap<ManaColor, Integer> bucket : buckets.values()) {
            bucket.keySet().removeIf(color -> !protectedColors.contains(color));
        }
        buckets.values().removeIf(EnumMap::isEmpty);
    }

    /**
     * Clears all persistent mana tracking. Called during end-of-turn cleanup
     * so subsequent drains will empty the pool normally.
     */
    public void clearPersistentMana() {
        for (ManaColor color : ManaColor.values()) {
            persistentMana.put(color, 0);
        }
    }

    public int getPersistentMana(ManaColor color) {
        return persistentMana.getOrDefault(color, 0);
    }

    public Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ManaColor color : ManaColor.values()) {
            int amount = pool.getOrDefault(color, 0);
            if (color == ManaColor.COLORLESS) {
                amount += artifactOnlyColorless + artifactAbilityOnlyColorless + myrOnlyColorless
                        + powerstoneOnlyColorless + legendarySpellOnlyColorless + instantSorceryOnlyColorless
                        + foretellOrInstantSorceryOnlyColorless + disturbOrInstantSorceryOnlyColorless
                        + foretellSpellOnlyColorless + xCostOnlyColorless
                        + colorlessSubtypeSpellOrAbilityMana.values().stream().mapToInt(Integer::intValue).sum()
                        + cumulativeUpkeepOnlyColorless;
            }
            amount += artifactOnlyMana.getOrDefault(color, 0);
            amount += artifactSpellOrAbilityOnlyMana.getOrDefault(color, 0);
            if (color == ManaColor.RED) {
                amount += restrictedRed;
            }
            if (color == ManaColor.GREEN) {
                amount += kickedOnlyGreen;
            }
            amount += getInstantSorceryOnlyColored(color);
            amount += getForetellSpellOnlyColored(color);
            amount += cumulativeUpkeepOnlyColored.getOrDefault(color, 0);
            amount += flashbackOnlyMana.getOrDefault(color, 0);
            amount += graveyardOnlyMana.getOrDefault(color, 0);
            amount += abilityOnlyMana.getOrDefault(color, 0);
            for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeOrLegendaryCreatureMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOrAbilityMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOnlyMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureSourceSpellOrAbilityMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeOrPlaneswalkerSpellMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            amount += partySpellOrAbilityMana.getOrDefault(color, 0);
            amount += creatureSpellOnlyMana.getOrDefault(color, 0);
            amount += creatureOrEnchantmentSpellOnlyMana.getOrDefault(color, 0);
            amount += creatureSpellOrAbilityMana.getOrDefault(color, 0);
            amount += manaValueAtLeastFourOnlyMana.getOrDefault(color, 0);
            amount += exiledSpellOnlyMana.getOrDefault(color, 0);
            map.put(color.getCode(), amount);
        }
        return map;
    }

    /**
     * Returns the total colored mana available across all pool buckets.
     * Colorless-only buckets (artifact-only, myr-only, etc.) are excluded.
     */
    public EnumMap<ManaColor, Integer> getColoredManaTotals() {
        EnumMap<ManaColor, Integer> totals = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            if (color == ManaColor.COLORLESS) {
                continue;
            }
            int amount = pool.getOrDefault(color, 0);
            amount += getInstantSorceryOnlyColored(color);
            amount += getForetellSpellOnlyColored(color);
            amount += cumulativeUpkeepOnlyColored.getOrDefault(color, 0);
            amount += flashbackOnlyMana.getOrDefault(color, 0);
            amount += graveyardOnlyMana.getOrDefault(color, 0);
            amount += abilityOnlyMana.getOrDefault(color, 0);
            if (color == ManaColor.RED) {
                amount += restrictedRed;
            }
            if (color == ManaColor.GREEN) {
                amount += kickedOnlyGreen;
            }
            amount += artifactOnlyMana.getOrDefault(color, 0);
            amount += artifactSpellOrAbilityOnlyMana.getOrDefault(color, 0);
            for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeOrLegendaryCreatureMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOrAbilityMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOnlyMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureSourceSpellOrAbilityMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeOrPlaneswalkerSpellMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            amount += partySpellOrAbilityMana.getOrDefault(color, 0);
            amount += creatureSpellOnlyMana.getOrDefault(color, 0);
            amount += creatureOrEnchantmentSpellOnlyMana.getOrDefault(color, 0);
            amount += creatureSpellOrAbilityMana.getOrDefault(color, 0);
            amount += manaValueAtLeastFourOnlyMana.getOrDefault(color, 0);
            for (EnumMap<ManaColor, Integer> colorMap : exiledCardOnlyMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            amount += exiledSpellOnlyMana.getOrDefault(color, 0);
            totals.put(color, amount);
        }
        return totals;
    }

    /**
     * Counts distinct colors of mana spent between two snapshots, including convoke contributions.
     * Colorless mana does not count toward Converge.
     */
    public static int countDistinctColoredManaSpent(EnumMap<ManaColor, Integer> before,
                                                   EnumMap<ManaColor, Integer> after,
                                                   Collection<ManaColor> convokeContributions) {
        EnumSet<ManaColor> colorsSpent = EnumSet.noneOf(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            if (color == ManaColor.COLORLESS) {
                continue;
            }
            int spent = before.getOrDefault(color, 0) - after.getOrDefault(color, 0);
            if (spent > 0) {
                colorsSpent.add(color);
            }
        }
        if (convokeContributions != null) {
            for (ManaColor color : convokeContributions) {
                if (color != null && color != ManaColor.COLORLESS) {
                    colorsSpent.add(color);
                }
            }
        }
        return colorsSpent.size();
    }

    /**
     * Returns the set of colors of mana spent between two snapshots, including convoke
     * contributions. Colorless is never included. Used by "if {C} was spent to cast this
     * spell" effects (e.g. Repel Intruders).
     */
    public static EnumSet<ManaColor> coloredManaColorsSpent(EnumMap<ManaColor, Integer> before,
                                                            EnumMap<ManaColor, Integer> after,
                                                            Collection<ManaColor> convokeContributions) {
        EnumSet<ManaColor> colorsSpent = EnumSet.noneOf(ManaColor.class);
        colorsSpent.addAll(coloredManaSpent(before, after, convokeContributions).keySet());
        return colorsSpent;
    }

    /**
     * Returns the amount of each color of mana spent between two snapshots, including convoke
     * contributions. Colorless is never included.
     */
    public static EnumMap<ManaColor, Integer> coloredManaSpent(EnumMap<ManaColor, Integer> before,
                                                               EnumMap<ManaColor, Integer> after,
                                                               Collection<ManaColor> convokeContributions) {
        EnumMap<ManaColor, Integer> manaSpent = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            if (color == ManaColor.COLORLESS) {
                continue;
            }
            int spent = before.getOrDefault(color, 0) - after.getOrDefault(color, 0);
            if (spent > 0) {
                manaSpent.put(color, spent);
            }
        }
        if (convokeContributions != null) {
            for (ManaColor color : convokeContributions) {
                if (color != null && color != ManaColor.COLORLESS) {
                    manaSpent.merge(color, 1, Integer::sum);
                }
            }
        }
        return manaSpent;
    }
}
