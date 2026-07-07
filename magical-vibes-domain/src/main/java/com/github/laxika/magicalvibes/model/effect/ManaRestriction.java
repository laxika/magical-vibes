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

    /** Colorless mana spendable only to cast artifact spells or activate abilities of artifacts (Grand Architect). */
    record ArtifactSpells() implements ManaRestriction {
        @Override
        public void applyTo(ManaPool pool, ManaColor color, int amount) {
            pool.addArtifactOnlyColorless(amount);
        }

        @Override
        public String description() {
            return "artifact spells only";
        }
    }

    /**
     * Colorless mana spendable only to cast spells / activate abilities of the given creature subtype.
     * Only Myr exists in the pool today (routes to the myr-only bucket, e.g. Myr Reservoir); the
     * subtype is retained as the routing/logging key.
     */
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
}
