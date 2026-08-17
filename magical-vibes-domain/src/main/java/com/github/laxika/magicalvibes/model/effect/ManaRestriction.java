package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

import java.util.Set;

/**
 * Spending restriction attached to mana produced by {@link AwardRestrictedManaEffect}. Each case
 * routes the produced mana into the matching {@link ManaPool} bucket. The spend side (ManaCost
 * payment, view serialization) keys on those typed buckets, not on this spec, so a restriction is a
 * pure routing key — the buckets remain the load-bearing store of restricted mana.
 */
public sealed interface ManaRestriction {

    /** Adds {@code amount} mana of {@code color} to the pool bucket for this restriction. */
    void applyTo(ManaPool pool, ManaColor color, int amount);

    /** Short human-readable description of the restriction, used in game log lines. */
    String description();

    /**
     * Mana spendable only on spells of the given card types. Instant+sorcery-only mana routes to the
     * instant/sorcery bucket (per-color or colorless); red creature-or-artifact-only mana (Geosurge)
     * routes to the restricted-red bucket; any other combination is added as plain mana.
     */
    record SpellTypes(Set<CardType> allowedSpellTypes) implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            if (allowedSpellTypes.contains(CardType.INSTANT)
                    && allowedSpellTypes.contains(CardType.SORCERY)
                    && !allowedSpellTypes.contains(CardType.CREATURE)
                    && !allowedSpellTypes.contains(CardType.ARTIFACT)) {
                if (color == ManaColor.COLORLESS) {
                    pool.addInstantSorceryOnlyColorless(amount);
                } else {
                    pool.addInstantSorceryOnlyColored(color, amount);
                }
            } else if (color == ManaColor.RED
                    && allowedSpellTypes.contains(CardType.CREATURE)
                    && allowedSpellTypes.contains(CardType.ARTIFACT)) {
                pool.addRestrictedRed(amount);
            } else {
                pool.add(color, amount);
            }
        }

        @Override
        public String description() {
            return allowedSpellTypes + " spells only";
        }
    }

    /** Mana spendable only to cast artifact spells or activate abilities of artifacts (Grand Architect). */
    record ArtifactSpells() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            if (color == ManaColor.COLORLESS) {
                pool.addArtifactOnlyColorless(amount);
            } else {
                pool.addArtifactOnlyMana(color, amount);
            }
        }

        @Override
        public String description() {
            return "artifact spells only";
        }
    }

    /** Mana spendable only to cast artifact spells or activate any activated ability (Guidelight Optimizer). */
    record ArtifactSpellsOrAbilities() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addArtifactSpellOrAbilityOnlyMana(color, amount);
        }

        @Override
        public String description() {
            return "artifact spells or abilities only";
        }
    }

    /**
     * Colorless mana spendable only to activate abilities of artifacts (Soldevi Machinist). Narrower than
     * {@link ArtifactSpells}: cannot pay artifact spell costs.
     */
    record ArtifactAbilities() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addArtifactAbilityOnlyColorless(amount);
        }

        @Override
        public String description() {
            return "artifact abilities only";
        }
    }

    /** Mana spendable only to pay ability costs, not to cast spells (Thran Turbine). */
    record Abilities() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addAbilityOnlyMana(color, amount);
        }

        @Override
        public String description() {
            return "abilities only";
        }
    }

    /** Mana that can't be spent to cast nonartifact spells (Powerstone tokens). */
    record Powerstone() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addPowerstoneOnlyColorless(amount);
        }

        @Override
        public String description() {
            return "nonartifact spells prohibited";
        }
    }

    /**
     * Colorless mana spendable only to cast legendary spells — any spell with the legendary supertype
     * (Untaidake, the Cloud Keeper). Spell-only: it cannot pay activation costs.
     */
    record LegendarySpells() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addLegendarySpellOnlyColorless(amount);
        }

        @Override
        public String description() {
            return "legendary spells only";
        }
    }

    /** Colorless mana spendable only to cast spells / activate abilities of Myr. */
    record SubtypeSpells(CardSubtype subtype) implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addMyrOnlyColorless(amount);
        }

        @Override
        public String description() {
            return subtype + " spells only";
        }
    }

    /** Colorless mana spendable only to cast colorless spells or activate abilities of the given subtype. */
    record ColorlessSubtypeSpellsOrAbilities(CardSubtype subtype) implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addColorlessSubtypeSpellOrAbilityMana(subtype, amount);
        }

        @Override
        public String description() {
            return "colorless " + subtype + " spells or abilities only";
        }
    }

    /**
     * Mana spendable only to cast creature spells of the given subtype (Gnarlroot Trapper: "Add
     * {G}. Spend this mana only to cast an Elf creature spell."). Routes into the per-subtype
     * creature-spell bucket shared with {@link AwardAnyColorChosenSubtypeCreatureManaEffect}, so —
     * unlike {@link SubtypeSpells} — it cannot pay for noncreature spells of that subtype or for
     * activated abilities.
     */
    record SubtypeCreatureSpells(CardSubtype subtype) implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addSubtypeCreatureMana(subtype, color, amount);
        }

        @Override
        public String description() {
            return subtype + " creature spells only";
        }
    }

    /** Mana spendable only to cast spells with {@code spellSubtype} or planeswalker spells of {@code planeswalkerSubtype}. */
    record SubtypeOrPlaneswalkerSpells(CardSubtype spellSubtype, CardSubtype planeswalkerSubtype) implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addSubtypeOrPlaneswalkerSpellMana(this, color, amount);
        }

        @Override
        public String description() {
            return spellSubtype + " or " + planeswalkerSubtype + " planeswalker spells only";
        }
    }

    /**
     * Colorless mana spendable only on costs that contain {X} (Rosheen Meanderer). Applies to any
     * spell or ability whose mana cost includes an {X} symbol; the mana can pay any generic portion
     * of such a cost. Stored in the x-cost-only colorless bucket.
     */
    record XCosts() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addXCostOnlyColorless(amount);
        }

        @Override
        public String description() {
            return "costs that contain {X} only";
        }
    }

    /** Mana spendable only to cast kicked spells (Elfhame Druid). Stored in the kicked-only bucket. */
    record KickedCosts() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addKickedOnlyGreen(amount);
        }

        @Override
        public String description() {
            return "kicked spells only";
        }
    }

    /**
     * Mana spendable only to pay cumulative upkeep costs (Adarkar Unicorn, Snowfall). Colorless
     * routes to the cumulative-upkeep-only colorless bucket; colored mana to the per-color bucket.
     */
    record CumulativeUpkeepCosts() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            if (color == ManaColor.COLORLESS) {
                pool.addCumulativeUpkeepOnlyColorless(amount);
            } else {
                pool.addCumulativeUpkeepOnlyColored(color, amount);
            }
        }

        @Override
        public String description() {
            return "cumulative upkeep costs only";
        }
    }
}
