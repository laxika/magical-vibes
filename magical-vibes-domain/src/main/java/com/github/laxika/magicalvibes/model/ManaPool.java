package com.github.laxika.magicalvibes.model;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ManaPool {

    private final EnumMap<ManaColor, Integer> pool = new EnumMap<>(ManaColor.class);
    private final EnumMap<ManaColor, Integer> creatureMana = new EnumMap<>(ManaColor.class);
    /**
     * Mana that may only be spent to cast spells (e.g. mana from lands tapped via Piracy). Tracked as a
     * tag on a subset of the regular {@link #pool}, mirroring {@link #creatureMana}: spell casting draws
     * from the regular pool as usual, while ability activation temporarily withdraws this mana so it can't
     * pay ability costs.
     */
    private final EnumMap<ManaColor, Integer> spellOnlyMana = new EnumMap<>(ManaColor.class);
    /** Mana that doesn't drain at step/phase transitions until end of turn (e.g. Grand Warlord Radha). */
    private final EnumMap<ManaColor, Integer> persistentMana = new EnumMap<>(ManaColor.class);
    private int artifactOnlyColorless;
    /** Colorless mana spendable only to activate abilities of artifacts (Soldevi Machinist). */
    private int artifactAbilityOnlyColorless;
    private int myrOnlyColorless;
    private int restrictedRed;
    private int kickedOnlyGreen;
    private int instantSorceryOnlyColorless;
    /** Colorless mana spendable only on costs that contain {X} (Rosheen Meanderer). */
    private int xCostOnlyColorless;
    /** Colorless mana spendable only to pay cumulative upkeep costs (Adarkar Unicorn, Snowfall). */
    private int cumulativeUpkeepOnlyColorless;
    /** Colored mana that can only be spent to cast instant or sorcery spells (e.g. Abstract Paintmage). */
    private final EnumMap<ManaColor, Integer> instantSorceryOnlyColored = new EnumMap<>(ManaColor.class);
    /** Colored mana spendable only to pay cumulative upkeep costs (Adarkar Unicorn). */
    private final EnumMap<ManaColor, Integer> cumulativeUpkeepOnlyColored = new EnumMap<>(ManaColor.class);
    /** Per-color mana that can only be spent to cast spells with flashback from a graveyard (e.g. Altar of the Lost). */
    private final EnumMap<ManaColor, Integer> flashbackOnlyMana = new EnumMap<>(ManaColor.class);
    /** Per-subtype, per-color mana that can only be spent to cast creature spells with a matching subtype (e.g. Pillar of Origins). */
    private final Map<CardSubtype, EnumMap<ManaColor, Integer>> subtypeCreatureMana = new HashMap<>();
    /**
     * Per-subtype, per-color mana that can only be spent to cast spells with a matching subtype OR to
     * activate abilities of permanents with that subtype (e.g. Smokebraider). Distinct from
     * {@link #subtypeCreatureMana}, which is spell-only.
     */
    private final Map<CardSubtype, EnumMap<ManaColor, Integer>> subtypeSpellOrAbilityMana = new HashMap<>();
    /**
     * Per-color mana that can only be spent to cast a creature spell of any type (e.g. Ancient
     * Ziggurat). Distinct from {@link #subtypeCreatureMana}, which is restricted to one chosen
     * creature subtype; this bucket pays for every creature spell.
     */
    private final EnumMap<ManaColor, Integer> creatureSpellOnlyMana = new EnumMap<>(ManaColor.class);
    /**
     * Permission flag (not mana): while set, white mana in this pool may additionally be spent to pay
     * red mana costs (Sunglasses of Urza — "you may spend white mana as though it were red mana"). Set
     * from board state at the payment/affordability sites; honored by {@link ManaCost#canPay}/{@code pay}.
     */
    private boolean whiteSpendableAsRed;

    public ManaPool() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
            creatureMana.put(color, 0);
            spellOnlyMana.put(color, 0);
            persistentMana.put(color, 0);
            flashbackOnlyMana.put(color, 0);
            instantSorceryOnlyColored.put(color, 0);
            cumulativeUpkeepOnlyColored.put(color, 0);
            creatureSpellOnlyMana.put(color, 0);
        }
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     */
    public ManaPool(ManaPool source) {
        pool.putAll(source.pool);
        creatureMana.putAll(source.creatureMana);
        spellOnlyMana.putAll(source.spellOnlyMana);
        persistentMana.putAll(source.persistentMana);
        flashbackOnlyMana.putAll(source.flashbackOnlyMana);
        this.artifactOnlyColorless = source.artifactOnlyColorless;
        this.artifactAbilityOnlyColorless = source.artifactAbilityOnlyColorless;
        this.myrOnlyColorless = source.myrOnlyColorless;
        this.restrictedRed = source.restrictedRed;
        this.kickedOnlyGreen = source.kickedOnlyGreen;
        this.instantSorceryOnlyColorless = source.instantSorceryOnlyColorless;
        this.xCostOnlyColorless = source.xCostOnlyColorless;
        this.cumulativeUpkeepOnlyColorless = source.cumulativeUpkeepOnlyColorless;
        instantSorceryOnlyColored.putAll(source.instantSorceryOnlyColored);
        cumulativeUpkeepOnlyColored.putAll(source.cumulativeUpkeepOnlyColored);
        for (Map.Entry<CardSubtype, EnumMap<ManaColor, Integer>> entry : source.subtypeCreatureMana.entrySet()) {
            subtypeCreatureMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        for (Map.Entry<CardSubtype, EnumMap<ManaColor, Integer>> entry : source.subtypeSpellOrAbilityMana.entrySet()) {
            subtypeSpellOrAbilityMana.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        creatureSpellOnlyMana.putAll(source.creatureSpellOnlyMana);
        this.whiteSpendableAsRed = source.whiteSpendableAsRed;
    }

    /** See {@link #whiteSpendableAsRed}. */
    public boolean isWhiteSpendableAsRed() {
        return whiteSpendableAsRed;
    }

    /** See {@link #whiteSpendableAsRed}. */
    public void setWhiteSpendableAsRed(boolean whiteSpendableAsRed) {
        this.whiteSpendableAsRed = whiteSpendableAsRed;
    }

    public void add(ManaColor color) {
        pool.merge(color, 1, Integer::sum);
    }

    public void add(ManaColor color, int amount) {
        pool.merge(color, amount, Integer::sum);
    }

    public void clear() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
            creatureMana.put(color, 0);
            spellOnlyMana.put(color, 0);
            flashbackOnlyMana.put(color, 0);
        }
        artifactOnlyColorless = 0;
        artifactAbilityOnlyColorless = 0;
        myrOnlyColorless = 0;
        restrictedRed = 0;
        kickedOnlyGreen = 0;
        instantSorceryOnlyColorless = 0;
        xCostOnlyColorless = 0;
        cumulativeUpkeepOnlyColorless = 0;
        for (ManaColor color : ManaColor.values()) {
            instantSorceryOnlyColored.put(color, 0);
            cumulativeUpkeepOnlyColored.put(color, 0);
            creatureSpellOnlyMana.put(color, 0);
        }
        subtypeCreatureMana.clear();
        subtypeSpellOrAbilityMana.clear();
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
        total += artifactOnlyColorless;
        total += artifactAbilityOnlyColorless;
        total += myrOnlyColorless;
        total += restrictedRed;
        total += kickedOnlyGreen;
        total += instantSorceryOnlyColorless;
        total += xCostOnlyColorless;
        total += cumulativeUpkeepOnlyColorless;
        total += getCumulativeUpkeepOnlyColoredTotal();
        total += getFlashbackOnlyManaTotal();
        for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOrAbilityMana.values()) {
            for (int value : colorMap.values()) {
                total += value;
            }
        }
        total += getCreatureSpellOnlyManaTotal();
        return total;
    }

    public void remove(ManaColor color) {
        pool.merge(color, -1, Integer::sum);
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

    public int getArtifactAbilityOnlyColorless() {
        return artifactAbilityOnlyColorless;
    }

    public void addArtifactAbilityOnlyColorless(int amount) {
        artifactAbilityOnlyColorless += amount;
    }

    public void removeArtifactAbilityOnlyColorless(int amount) {
        artifactAbilityOnlyColorless = Math.max(0, artifactAbilityOnlyColorless - amount);
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

    public int getInstantSorceryOnlyColorless() {
        return instantSorceryOnlyColorless;
    }

    public void addInstantSorceryOnlyColorless(int amount) {
        instantSorceryOnlyColorless += amount;
    }

    public void removeInstantSorceryOnlyColorless(int amount) {
        instantSorceryOnlyColorless = Math.max(0, instantSorceryOnlyColorless - amount);
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
        return instantSorceryOnlyColored.getOrDefault(color, 0);
    }

    public int getInstantSorceryOnlyColoredTotal() {
        int total = 0;
        for (int value : instantSorceryOnlyColored.values()) {
            total += value;
        }
        return total;
    }

    public void addInstantSorceryOnlyColored(ManaColor color, int amount) {
        instantSorceryOnlyColored.merge(color, amount, Integer::sum);
    }

    public void removeInstantSorceryOnlyColored(ManaColor color, int amount) {
        int current = instantSorceryOnlyColored.getOrDefault(color, 0);
        instantSorceryOnlyColored.put(color, Math.max(0, current - amount));
    }

    public void addSubtypeCreatureMana(CardSubtype subtype, ManaColor color, int amount) {
        subtypeCreatureMana.computeIfAbsent(subtype, k -> {
            EnumMap<ManaColor, Integer> m = new EnumMap<>(ManaColor.class);
            for (ManaColor c : ManaColor.values()) m.put(c, 0);
            return m;
        }).merge(color, amount, Integer::sum);
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
            }
        }
    }

    public void addSubtypeSpellOrAbilityMana(CardSubtype subtype, ManaColor color, int amount) {
        subtypeSpellOrAbilityMana.computeIfAbsent(subtype, k -> {
            EnumMap<ManaColor, Integer> m = new EnumMap<>(ManaColor.class);
            for (ManaColor c : ManaColor.values()) m.put(c, 0);
            return m;
        }).merge(color, amount, Integer::sum);
    }

    /** Total spell-or-ability mana of the given color available across all matching subtypes. */
    public int getSubtypeSpellOrAbilityManaForColor(Set<CardSubtype> subtypes, ManaColor color) {
        int total = 0;
        for (CardSubtype subtype : subtypes) {
            EnumMap<ManaColor, Integer> colorMap = subtypeSpellOrAbilityMana.get(subtype);
            if (colorMap != null) {
                total += colorMap.getOrDefault(color, 0);
            }
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

    /**
     * Adds mana that persists through step/phase transitions until end of turn.
     * The mana is added to both the regular pool and the persistent tracker.
     */
    public void addPersistentMana(ManaColor color, int amount) {
        pool.merge(color, amount, Integer::sum);
        persistentMana.merge(color, amount, Integer::sum);
    }

    /**
     * Drains all non-persistent mana. For each color, the pool is reduced to
     * at most the persistent amount. Persistent mana survives step/phase transitions.
     */
    public void drainNonPersistent() {
        for (ManaColor color : ManaColor.values()) {
            int persistent = persistentMana.getOrDefault(color, 0);
            int current = pool.getOrDefault(color, 0);
            // Keep the lesser of current pool and persistent amount
            pool.put(color, Math.min(current, persistent));
            // Clamp persistent to not exceed what's in the pool
            persistentMana.put(color, Math.min(current, persistent));
        }
        // Clamp creature mana to not exceed pool totals
        for (ManaColor color : ManaColor.values()) {
            int total = pool.getOrDefault(color, 0);
            int creature = creatureMana.getOrDefault(color, 0);
            creatureMana.put(color, Math.min(creature, total));
            int spellOnly = spellOnlyMana.getOrDefault(color, 0);
            spellOnlyMana.put(color, Math.min(spellOnly, total));
        }
        artifactOnlyColorless = 0;
        artifactAbilityOnlyColorless = 0;
        myrOnlyColorless = 0;
        restrictedRed = 0;
        kickedOnlyGreen = 0;
        instantSorceryOnlyColorless = 0;
        xCostOnlyColorless = 0;
        cumulativeUpkeepOnlyColorless = 0;
        for (ManaColor color : ManaColor.values()) {
            flashbackOnlyMana.put(color, 0);
            instantSorceryOnlyColored.put(color, 0);
            cumulativeUpkeepOnlyColored.put(color, 0);
            creatureSpellOnlyMana.put(color, 0);
        }
        subtypeCreatureMana.clear();
        subtypeSpellOrAbilityMana.clear();
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
                        + instantSorceryOnlyColorless + xCostOnlyColorless + cumulativeUpkeepOnlyColorless;
            }
            if (color == ManaColor.RED) {
                amount += restrictedRed;
            }
            if (color == ManaColor.GREEN) {
                amount += kickedOnlyGreen;
            }
            amount += instantSorceryOnlyColored.getOrDefault(color, 0);
            amount += cumulativeUpkeepOnlyColored.getOrDefault(color, 0);
            amount += flashbackOnlyMana.getOrDefault(color, 0);
            for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOrAbilityMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            amount += creatureSpellOnlyMana.getOrDefault(color, 0);
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
            amount += instantSorceryOnlyColored.getOrDefault(color, 0);
            amount += cumulativeUpkeepOnlyColored.getOrDefault(color, 0);
            amount += flashbackOnlyMana.getOrDefault(color, 0);
            if (color == ManaColor.RED) {
                amount += restrictedRed;
            }
            if (color == ManaColor.GREEN) {
                amount += kickedOnlyGreen;
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeCreatureMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            for (EnumMap<ManaColor, Integer> colorMap : subtypeSpellOrAbilityMana.values()) {
                amount += colorMap.getOrDefault(color, 0);
            }
            amount += creatureSpellOnlyMana.getOrDefault(color, 0);
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
        return colorsSpent;
    }
}
