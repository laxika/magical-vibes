package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public sealed interface ChoiceContext {

    record SagaChapterCounterAssignment(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                         UUID sourcePermanentId, String chapterName, CounterType counterType,
                                         List<UUID> targetIds, Map<UUID, Integer> assignments, int total,
                                         int nextTargetIndex) implements ChoiceContext {

        public SagaChapterCounterAssignment {
            effects = List.copyOf(effects);
            targetIds = List.copyOf(targetIds);
            assignments = Map.copyOf(new java.util.LinkedHashMap<>(assignments));
        }
    }

    record CounterDistributionAssignment(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, CounterType counterType,
                                          List<UUID> targetIds, Map<UUID, Integer> assignments, int total,
                                          int nextTargetIndex) implements ChoiceContext {

        public CounterDistributionAssignment {
            effects = List.copyOf(effects);
            targetIds = List.copyOf(targetIds);
            assignments = Map.copyOf(new java.util.LinkedHashMap<>(assignments));
        }
    }

    record TextChangeFromWord(UUID targetId, boolean untilEndOfTurn) implements ChoiceContext {}

    record TextChangeToWord(UUID targetId, String fromWord, boolean isColor, boolean untilEndOfTurn)
            implements ChoiceContext {}

    record ManaColorSpellChoice(UUID playerId, int amount, Set<CardSubtype> subtypes) implements ChoiceContext {
        public ManaColorSpellChoice {
            subtypes = Set.copyOf(subtypes);
        }
    }

    record RestrictedManaColorChoice(UUID playerId, int amount, boolean fromCreature,
                                     List<ManaColor> fixedColorOptions,
                                     ManaRestriction restriction) implements ChoiceContext {
        public RestrictedManaColorChoice {
            fixedColorOptions = List.copyOf(fixedColorOptions);
        }
    }

    record PersistentManaColorChoice(UUID playerId, int amount) implements ChoiceContext {}
    record ExiledSpellManaColorChoice(UUID playerId, boolean fromCreature, int amount)
            implements ChoiceContext {}
    record GraveyardManaColorChoice(UUID playerId, boolean fromCreature, int amount) implements ChoiceContext {}
    record SpellOnlyManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                    boolean anyColorCombination, UUID recipientPlayerId)
            implements ChoiceContext {

        public SpellOnlyManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                                        boolean anyColorCombination) {
            this(playerId, fromCreature, amount, anyColorCombination, null);
        }

        public SpellOnlyManaColorChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new SpellOnlyManaColorChoice(playerId, fromCreature, amount,
                    anyColorCombination, recipientPlayerId);
        }
    }

    record ChosenPlayerManaColorChoice(UUID playerId, UUID sourceControllerId, UUID recipientPlayerId,
                                       boolean fromCreature, int amount) implements ChoiceContext {}

    record ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                           boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                           boolean creatureSourceSpellOrAbility,
                           List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                           boolean artifactSpellOrAbilityOnly,
                           boolean grantsUncounterable, boolean manaValueAtLeastFour,
                           boolean creatureSpellOrAbilityOnly,
                           UUID sourcePermanentId,
                           Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                           boolean abilityOnly,
                           UUID recipientPlayerId,
                           boolean grantsAdditionalPlusOneCounter,
                           boolean fromSnowSource,
                           boolean fromCaveSource,
                           boolean grantsRiot,
                           ManaRestriction.SubtypeOrPlaneswalkerSpells restrictedToSubtypeSpell,
                           boolean differentColors,
                           boolean planeswalkerSpellOnly) implements ChoiceContext {

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes, boolean abilityOnly,
                               UUID recipientPlayerId, boolean grantsAdditionalPlusOneCounter,
                               boolean fromSnowSource, boolean grantsRiot) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                     sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                     recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource, false, grantsRiot,
                     null, false, false);
         }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes, boolean abilityOnly,
                               UUID recipientPlayerId, boolean grantsAdditionalPlusOneCounter,
                               boolean fromSnowSource) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                    recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    false, false, null, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                               boolean abilityOnly, UUID recipientPlayerId,
                               boolean grantsAdditionalPlusOneCounter) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                    recipientPlayerId, grantsAdditionalPlusOneCounter, false,
                    false, false, null, false, false);
        }

        public ManaColorChoice withSnowSource(boolean fromSnowSource) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly);
        }

        public ManaColorChoice withCaveSource(boolean fromCaveSource) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype,
                    creatureSourceSpellOrAbility, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, restrictedToSpellOrAbilitySubtypes,
                    abilityOnly, recipientPlayerId, grantsAdditionalPlusOneCounter, fromSnowSource,
                    fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                               boolean artifactSpellOrAbilityOnly, boolean grantsUncounterable,
                               boolean manaValueAtLeastFour, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes, boolean abilityOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, false, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                               boolean artifactSpellOrAbilityOnly, boolean grantsUncounterable,
                               boolean manaValueAtLeastFour, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, false, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, false, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                               boolean grantsAdditionalPlusOneCounter) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, false, null,
                    grantsAdditionalPlusOneCounter);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount,
                               CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly,
                               boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               boolean creatureSourceSpellOrAbility, List<ManaColor> fixedColorOptions,
                               boolean creatureSpellOnly, boolean artifactSpellOrAbilityOnly,
                               boolean grantsUncounterable, boolean manaValueAtLeastFour,
                               boolean creatureSpellOrAbilityOnly, UUID sourcePermanentId,
                               Set<CardSubtype> restrictedToSpellOrAbilitySubtypes,
                               boolean abilityOnly, UUID recipientPlayerId) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly,
                    instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly,
                    grantsUncounterable, manaValueAtLeastFour, creatureSpellOrAbilityOnly,
                    sourcePermanentId, restrictedToSpellOrAbilitySubtypes, abilityOnly,
                    recipientPlayerId, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               List<ManaColor> fixedColorOptions, boolean creatureSpellOnly,
                               boolean artifactSpellOrAbilityOnly, boolean grantsUncounterable,
                               boolean manaValueAtLeastFour, boolean creatureSpellOrAbilityOnly,
                               UUID sourcePermanentId) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, fixedColorOptions, creatureSpellOnly,
                    artifactSpellOrAbilityOnly, grantsUncounterable, manaValueAtLeastFour,
                    creatureSpellOrAbilityOnly, sourcePermanentId, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                               List<ManaColor> fixedColorOptions, boolean creatureSpellOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, fixedColorOptions, creatureSpellOnly,
                    false, false, false, false, null, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
                    spellOrAbilitySubtype, false, null, false, false, false, false, false, null, null, false);
        }

        public ManaColorChoice withSourcePermanentId(UUID sourcePermanentId) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions,
                    creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot,
                    restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly);
        }

        public ManaColorChoice withAdditionalPlusOneCounter() {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId, true,
                    fromSnowSource, fromCaveSource, grantsRiot, restrictedToSubtypeSpell,
                    differentColors, planeswalkerSpellOnly);
        }

        public ManaColorChoice withRiot() {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, true,
                    restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly);
        }

        public ManaColorChoice withRecipientPlayerId(UUID recipientPlayerId) {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions,
                    creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot,
                    restrictedToSubtypeSpell, differentColors, planeswalkerSpellOnly);
        }

        public ManaColorChoice withPlaneswalkerSpellOnly() {
            return new ManaColorChoice(playerId, fromCreature, amount, restrictedToCreatureSubtype,
                    flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, creatureSourceSpellOrAbility,
                    fixedColorOptions, creatureSpellOnly, artifactSpellOrAbilityOnly, grantsUncounterable,
                    manaValueAtLeastFour, creatureSpellOrAbilityOnly, sourcePermanentId,
                    restrictedToSpellOrAbilitySubtypes, abilityOnly, recipientPlayerId,
                    grantsAdditionalPlusOneCounter, fromSnowSource, fromCaveSource, grantsRiot,
                    restrictedToSubtypeSpell, differentColors, true);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature) {
            this(playerId, fromCreature, 1, null, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount) {
            this(playerId, fromCreature, amount, null, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly, boolean instantSorceryOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly, false);
        }

        /** "Add N mana of any one color, spendable only to cast instant/sorcery spells" (e.g. Resonating Lute). */
        public static ManaColorChoice instantSorceryOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, true, false);
        }

        /**
         * "Add N mana in any combination of colors, spendable only to cast spells of {@code subtype}
         * or activate abilities of permanents of that subtype" (e.g. Smokebraider). Each mana's color
         * is chosen individually (any combination).
         */
        public static ManaColorChoice subtypeSpellOrAbility(UUID playerId, int amount, CardSubtype subtype) {
            return new ManaColorChoice(playerId, false, amount, subtype, false, false, true);
        }

        public static ManaColorChoice subtypeSpellOnly(UUID playerId, int amount,
                                                        ManaRestriction.SubtypeOrPlaneswalkerSpells restriction) {
            return new ManaColorChoice(playerId, false, amount, null, false, false, false,
                    false, null, false, false, false, false, false, null, null, false, null,
                    false, false, false, false, restriction, false, false);
        }

        /** "Add one mana of any color" restricted to the four party creature types. */
        public static ManaColorChoice partySpellOrAbility(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, false, true,
                    false, null, false, false, false, false, false, null,
                    Set.of(CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD), false);
        }

        /**
         * "Add N mana of any color, spendable only to cast creature spells of the chosen type or
         * activate abilities of creature sources of the chosen type" (Secluded Courtyard).
         */
        public static ManaColorChoice creatureSourceSpellOrAbility(UUID playerId, int amount,
                                                                     CardSubtype subtype) {
            return new ManaColorChoice(playerId, false, amount, subtype, false, false, false,
                    true, null, false, false, false, false, false, null, null, false);
        }

        /**
         * "Add N mana, each chosen individually from a fixed list of colors" (filter lands such as
         * Fire-Lit Thicket's "Add {R}{R}, {R}{G}, or {G}{G}"). Each mana's color is picked separately
         * from {@code colors}, re-prompting until all {@code amount} have been chosen.
         */
        public static ManaColorChoice fixedColorCombination(UUID playerId, boolean fromCreature, int amount, List<ManaColor> colors) {
            return new ManaColorChoice(playerId, fromCreature, amount, null, false, false, false, colors, false);
        }

        public static ManaColorChoice riotColorCombination(UUID playerId, boolean fromCreature,
                                                            int amount, List<ManaColor> colors) {
            return fixedColorCombination(playerId, fromCreature, amount, colors).withRiot();
        }

        /** "Add N mana of different colors." Each subsequent pick excludes previous picks. */
        public static ManaColorChoice differentColors(UUID playerId, boolean fromCreature, int amount,
                                                       List<ManaColor> colors) {
            return new ManaColorChoice(playerId, fromCreature, amount, null, false, false, false,
                    false, colors, false, false, false, false, false, null, null, false, null,
                    false, false, false, false, null, true, false);
        }

        /** "Add N mana of different colors, spendable only to cast planeswalker spells." */
        public static ManaColorChoice planeswalkerSpellOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount).withPlaneswalkerSpellOnly();
        }

        /**
         * "Add N mana of any one color, spendable only to cast a creature spell of any type"
         * (Ancient Ziggurat). The color is chosen at activation; the mana routes to the pool's
         * creature-spell-only bucket.
         */
        public static ManaColorChoice creatureSpellOnly(UUID playerId, int amount) {
            return creatureSpellOnly(playerId, false, amount);
        }

        public static ManaColorChoice creatureSpellOnly(UUID playerId, boolean fromCreature, int amount) {
            return new ManaColorChoice(playerId, fromCreature, amount, null, false, false, false, null, true);
        }

        /** "Add N mana of any one color, spendable only to cast creature spells or activate abilities of creature sources" (Gwenna, Eyes of Gaea). */
        public static ManaColorChoice creatureSpellOrAbilityOnly(UUID playerId, int amount) {
            return creatureSpellOrAbilityOnly(playerId, false, amount);
        }

        public static ManaColorChoice creatureSpellOrAbilityOnly(UUID playerId, boolean fromCreature, int amount) {
            return new ManaColorChoice(
                    playerId, fromCreature, amount, null, false, false, false, false, null,
                    false, false, false, false, true, null, null, false, null);
        }

        /** "Add one mana of any of this creature's colors, spendable only to activate creature abilities." */
        public static ManaColorChoice creatureAbilityOnly(UUID playerId, boolean fromCreature, int amount,
                                                          List<ManaColor> colors) {
            return new ManaColorChoice(
                    playerId, fromCreature, amount, null, false, false, false, false, colors,
                    false, false, false, false, false, null, null, true, null);
        }

        /** "Add N mana of any one color, spendable only to activate abilities." */
        public static ManaColorChoice abilityOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, false, false, false,
                    null, false, false, false, false, false, null, null, true, null);
        }

        /** "Add N mana of any one color, spendable only to cast artifact spells or activate abilities of artifacts". */
        public static ManaColorChoice artifactSpellOrAbilityOnly(UUID playerId, int amount) {
            return new ManaColorChoice(
                    playerId, false, amount, null, false, false, false, false, null,
                    false, true, false, false, false, null, null, false);
        }

        /**
         * "Add one mana of any color, spendable only to cast a creature spell of the chosen type, and
         * that spell can't be countered" (Cavern of Souls). The mana routes to the pool's
         * subtype-creature bucket and is additionally marked as uncounterable-granting.
         */
        public static ManaColorChoice chosenSubtypeCreatureUncounterable(UUID playerId, int amount, CardSubtype subtype) {
            return new ManaColorChoice(
                    playerId, false, amount, subtype, false, false, false, false, null,
                    false, false, true, false, false, null, null, false);
        }

        /** "Add N mana of any one color, spendable only to cast spells with mana value 4 or greater." */
        public static ManaColorChoice manaValueAtLeastFour(UUID playerId, int amount) {
            return new ManaColorChoice(
                    playerId, false, amount, null, false, false, false, false, null,
                    false, false, false, true, false, null, null, false);
        }
    }

    record DifferentColorManaChoice(UUID playerId, int amount, ManaSpendRestriction restriction,
                                    ManaColor firstColor) implements ChoiceContext {}

    /** A mana ability that adds mana equal to the chosen color's devotion. */
    record DevotionManaColorChoice(UUID playerId, UUID sourcePermanentId, boolean fromCreature,
                                   int manaMultiplier) implements ChoiceContext {
    }

    record DrawReplacementChoice(UUID playerId, DrawReplacementKind kind) implements ChoiceContext {}

    record CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes,
                          boolean nonbasicLandOnly, UUID attachedTo) implements ChoiceContext {

        public CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes) {
            this(card, controllerId, excludedTypes, false, null);
        }

        public CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes,
                              boolean nonbasicLandOnly) {
            this(card, controllerId, excludedTypes, nonbasicLandOnly, null);
        }
    }

    record CardTypeOnEnterChoice(Card card, UUID controllerId, List<CardType> excludedTypes)
            implements ChoiceContext {}

    /**
     * "You and an opponent each choose a card name other than a basic land card name" as the source
     * enters (Null Chamber). {@code choosingPlayerId} is whoever is being asked right now:
     * {@code firstChosenName} is {@code null} on the controller's pick and holds it on the
     * opponent's follow-up pick, after which the permanent enters carrying both names.
     */
    record DualCardNameChoice(Card card, UUID controllerId, UUID choosingPlayerId,
                              String firstChosenName) implements ChoiceContext {}

    record KeywordGrantChoice(UUID targetId, List<Keyword> options) implements ChoiceContext {}

    /** Choosing a basic land type for a plain landwalk grant until end of turn. */
    record LandwalkGrantChoice(UUID targetId) implements ChoiceContext {}

    record ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes,
                              int maxCount, boolean drawForHandExiled,
                              com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                              String sourceSetCode) implements ChoiceContext {

        public ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes) {
            this(targetPlayerId, controllerId, excludedTypes, Integer.MAX_VALUE, false, null, null);
        }

        public ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes,
                                 boolean drawForHandExiled) {
            this(targetPlayerId, controllerId, excludedTypes, Integer.MAX_VALUE, drawForHandExiled, null, null);
        }

        public ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes,
                                 int maxCount, boolean drawForHandExiled) {
            this(targetPlayerId, controllerId, excludedTypes, maxCount, drawForHandExiled, null, null);
        }

        public boolean drawForEachHandCardExiled() {
            return drawForHandExiled;
        }
    }

    /**
     * The controller chose a card name; {@code targetPlayerId} reveals their hand, the source deals
     * {@code damagePerCard} damage per revealed copy, then every copy in their hand/graveyard/library
     * is exiled and they shuffle (Thought Hemorrhage). When {@code chooseAnyNumber} is true, the
     * selected card is exiled first and the controller chooses any number of the remaining copies
     * (Pick the Brain). {@code sourceCard} attributes the damage.
     */
    record RevealHandDamageAndExileByNameChoice(UUID targetPlayerId, UUID controllerId,
                                                List<CardType> excludedTypes, int damagePerCard,
                                                Card sourceCard, boolean chooseAnyNumber) implements ChoiceContext {
        public RevealHandDamageAndExileByNameChoice(UUID targetPlayerId, UUID controllerId,
                                                     List<CardType> excludedTypes, int damagePerCard,
                                                     Card sourceCard) {
            this(targetPlayerId, controllerId, excludedTypes, damagePerCard, sourceCard, false);
        }
    }

    /** Assembly Hall: choose the name of a creature card currently in your hand to reveal. */
    record AssemblyHallCreatureCardChoice(UUID controllerId) implements ChoiceContext {}

    /**
     * First half of Mindblaze: the controller picks a card name. The answer chains into
     * {@link RevealLibraryNumberGuessChoice}, which asks for the number.
     */
    record RevealLibraryNameGuessChoice(UUID targetPlayerId, UUID controllerId,
                                        List<CardType> excludedTypes, int damage,
                                        Card sourceCard) implements ChoiceContext {}

    /**
     * Second half of Mindblaze: the controller picks a number greater than 0 for the already
     * chosen {@code chosenName}. On the answer {@code targetPlayerId} reveals their library, takes
     * {@code damage} damage if it holds exactly that many cards with that name, and shuffles.
     */
    record RevealLibraryNumberGuessChoice(UUID targetPlayerId, UUID controllerId, String chosenName,
                                          int damage, Card sourceCard) implements ChoiceContext {}

    /** The controller names a card, then the targeted opponent guesses whether that name is in the controller's hand. */
    record LiarsPendulumChoice(UUID controllerId, UUID targetPlayerId, UUID sourcePermanentId,
                               Card sourceCard, String chosenName) implements ChoiceContext {}

    /**
     * A single protection choice that applies to every permanent in {@code targetIds} — one pick
     * covering all of a spell's targets ("X target creatures gain protection from the chosen
     * color", Prismatic Boon), which for most cards is a one-element list.
     */
    record ProtectionColorChoice(List<UUID> targetIds, boolean includeArtifacts, boolean includeColorless)
            implements ChoiceContext {

        public ProtectionColorChoice(UUID targetId, boolean includeArtifacts) {
            this(List.of(targetId), includeArtifacts, false);
        }

        public ProtectionColorChoice(List<UUID> targetIds, boolean includeArtifacts) {
            this(targetIds, includeArtifacts, false);
        }
    }

    record PreventDamageToTargetFromChosenColorChoice(UUID targetId) implements ChoiceContext {}

    record TargetCreatureHexproofFromChosenColorChoice(UUID targetId) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution; the target permanent then becomes that color
     * until end of turn (CR 105.3 / layer 5). Used by Distorting Lens.
     *
     * @param targetId       the permanent that becomes the chosen color
     * @param controllerId   controller of the ability that created the effect
     * @param sourceCardName name of the card whose ability created the effect
     */
    record ColorSetChoice(UUID targetId, UUID controllerId, String sourceCardName) implements ChoiceContext {}

    /**
     * The controller chooses one color for all targeted creatures until end of turn.
     */
    record ColorSetTargetsChoice(List<UUID> targetIds, UUID controllerId, String sourceCardName)
            implements ChoiceContext {}

    /**
     * A single color choice that grants the chosen controller and each permanent they control
     * protection from the chosen color until end of turn (e.g. Faith's Shield fateful hour).
     */
    record MassProtectionColorChoice(UUID controllerId) implements ChoiceContext {}

    record SubtypeChoice(UUID permanentId, boolean landPlay) implements ChoiceContext {
        public SubtypeChoice(UUID permanentId) {
            this(permanentId, false);
        }
    }

    record SourceSubtypeChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * The controller chooses a creature type at resolution for a spell/ability that has no
     * permanent to store it on (e.g. Coordinated Barrage). The answer is stored on
     * {@code GameData.chosenSpellSubtype} and effect resolution resumes.
     */
    record SpellCreatureTypeChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing a card type at resolution for a spell with no permanent to store it on. */
    record SpellCardTypeChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing a color at resolution for a spell with no permanent to store it on. */
    record SpellColorChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing a number at resolution for a spell with no permanent to store it on. */
    record SpellNumberChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing odd or even "as this permanent enters" (Ashling's Prerogative). */
    record ManaValueParityChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * Choosing a number in an inclusive range for {@code permanentId} (e.g. Shapeshifter's "choose a
     * number between 0 and 7", both as it enters and at each upkeep). The answer is stored on the
     * permanent via {@code Permanent.setChosenNumber(int)}.
     */
    record NumberChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * "As this creature enters, pay any amount of life" (Minion of the Wastes). The controller
     * picks 0..the offered maximum as {@code permanentId} enters; on the answer that much life is
     * paid, the amount is stored via {@code Permanent.setChosenNumber(int)}, and the deferred
     * enter-the-battlefield triggers of {@code card} are processed.
     */
    record PayAnyAmountOfLifeAsEnters(UUID permanentId, UUID controllerId, Card card, UUID targetId,
                                      boolean wasCastFromHand, int etbMode,
                                      boolean kicked) implements ChoiceContext {}

    /** The controller chooses the counter type for a creature's as-enters replacement. */
    record AsEntersCounterTypeChoice(UUID permanentId, UUID controllerId, Card card, UUID targetId,
                                     boolean wasCastFromHand, int etbMode, int xValue, boolean kicked,
                                     List<UUID> targetIds, int exiledCardCount,
                                     List<CounterType> counterTypes) implements ChoiceContext {
        public AsEntersCounterTypeChoice {
            targetIds = List.copyOf(targetIds);
            counterTypes = List.copyOf(counterTypes);
        }

        public static String label(CounterType counterType) {
            return switch (counterType) {
                case PLUS_TWO_PLUS_ZERO -> "+2/+0";
                case PLUS_ONE_PLUS_ONE -> "+1/+1";
                case PLUS_ZERO_PLUS_TWO -> "+0/+2";
                default -> counterType.name().toLowerCase().replace('_', ' ');
            };
        }
    }

    /**
     * Choosing how many {@code counterType} counters to remove from {@code permanentId} as a
     * storage land's mana ability resolves (0..the count present). On resume the chosen number of
     * counters is removed and that much mana of {@code color} is added to {@code playerId}'s pool
     * (times {@code manaMultiplier} for Mana Reflection; {@code fromCreature} marks creature mana).
     * Used by the storage-land cycle via {@code RemoveCountersForManaEffect}.
     */
    record RemoveCountersForManaChoice(UUID playerId, UUID permanentId, List<ManaColor> colors,
                                       CounterType counterType, boolean fromCreature,
                                       int manaMultiplier) implements ChoiceContext {

        public RemoveCountersForManaChoice {
            colors = List.copyOf(colors);
        }

        public RemoveCountersForManaChoice(UUID playerId, UUID permanentId, ManaColor color,
                                           CounterType counterType, boolean fromCreature,
                                           int manaMultiplier) {
            this(playerId, permanentId, List.of(color), counterType, fromCreature, manaMultiplier);
        }
    }

    /**
     * Tetravus first upkeep trigger: the controller chooses how many of {@code permanentId}'s +1/+1
     * counters (0..the count present) to remove; on the answer that many are removed and that many
     * Tetravite tokens are created from {@code tokenTemplate}, each recorded as "created with" the
     * source in {@code GameData.sourceCreatedTokens} (read by the paired exile trigger).
     */
    record TetravusCounterRemoval(UUID permanentId,
                                  com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate)
            implements ChoiceContext {}

    /**
     * "Move any number of {@code counterType} counters from target creature onto another target
     * creature" (Bioshift): the spell's controller chooses how many (0..the count on
     * {@code fromPermanentId}); on the answer that many counters are moved onto
     * {@code toPermanentId}.
     */
    record MoveCountersAmountChoice(UUID fromPermanentId, UUID toPermanentId, CounterType counterType,
                                    String sourceCardName) implements ChoiceContext {}

    /**
     * Aetherborn Marauder: the controller chooses how many counters to move from each selected
     * eligible permanent onto the source. The sequence advances to the next permanent after every
     * answer, so zero is a valid choice for each one.
     */
    record MoveCountersFromControlledPermanentsAmountChoice(List<UUID> fromPermanentIds, int index,
                                                             UUID toPermanentId, CounterType counterType,
                                                             String sourceCardName, String fromCardName,
                                                             int countersPerMovedCounter, int countersRemoved)
            implements ChoiceContext {
        public MoveCountersFromControlledPermanentsAmountChoice(List<UUID> fromPermanentIds, int index,
                                                                 UUID toPermanentId, CounterType counterType,
                                                                 String sourceCardName, String fromCardName) {
            this(fromPermanentIds, index, toPermanentId, counterType, sourceCardName, fromCardName, 1, 0);
        }

        public MoveCountersFromControlledPermanentsAmountChoice(List<UUID> fromPermanentIds, int index,
                                                                 UUID toPermanentId, CounterType counterType,
                                                                 String sourceCardName, String fromCardName,
                                                                 int countersPerMovedCounter) {
            this(fromPermanentIds, index, toPermanentId, counterType, sourceCardName, fromCardName,
                    countersPerMovedCounter, 0);
        }

        public MoveCountersFromControlledPermanentsAmountChoice {
            fromPermanentIds = List.copyOf(fromPermanentIds);
            if (countersPerMovedCounter < 1 || countersRemoved < 0) {
                throw new IllegalArgumentException("counter choice values must be non-negative and valid");
            }
        }
    }

    /** Choosing one of Primal Clay's three shapes "as this creature enters". */
    record PrimalClayFormChoice(UUID permanentId) implements ChoiceContext {}

    /** Choosing a base power/toughness form as a permanent enters or is turned face up. */
    record PowerToughnessFormChoice(UUID permanentId, List<PowerToughnessForm> forms,
                                     boolean turnFaceUp) implements ChoiceContext {
        public PowerToughnessFormChoice {
            forms = List.copyOf(forms);
        }
    }

    /**
     * Choosing a basic land type "as ~ enters". When {@code chainSecondAfter} is true, answering
     * the first pick immediately begins a second pick ({@code isSecondChoice=true}) for cards that
     * choose two types (Illusionary Terrain). The second pick stores into
     * {@code Permanent.secondChosenSubtype}. {@code allowedTypes} narrows the offered types for
     * cards that only allow some of them ("choose Island or Swamp" — Roots of Life); an empty list
     * offers all five.
     */
    record BasicLandTypeChoice(UUID permanentId, boolean isSecondChoice, boolean chainSecondAfter,
                               List<CardSubtype> allowedTypes) implements ChoiceContext {
        public BasicLandTypeChoice(UUID permanentId) {
            this(permanentId, false, false, List.of());
        }

        public BasicLandTypeChoice(UUID permanentId, boolean isSecondChoice, boolean chainSecondAfter) {
            this(permanentId, isSecondChoice, chainSecondAfter, List.of());
        }
    }

    /**
     * Choosing a basic land type for a target land: either added "in addition to its other types"
     * (Navigator's Compass) or, when {@code replacing} is {@code true}, replacing the land's other
     * types and mana ability per rule 305.7 (Tideshaper Mystic).
     *
     * @param targetLandId the target land that gains/becomes the chosen basic land type
     * @param duration     how long the granted/overriding type lasts
     * @param replacing    {@code true} to replace the land's types, {@code false} to add
     * @param allowedTypes the offered basic land types; an empty list offers all five
     */
    record AddBasicLandTypeChoice(UUID targetLandId, EffectDuration duration, boolean replacing,
                                  List<CardSubtype> allowedTypes) implements ChoiceContext {

        public AddBasicLandTypeChoice {
            allowedTypes = allowedTypes == null ? List.of() : List.copyOf(allowedTypes);
        }

        public AddBasicLandTypeChoice(UUID targetLandId, EffectDuration duration, boolean replacing) {
            this(targetLandId, duration, replacing, List.of());
        }

        public AddBasicLandTypeChoice(UUID targetLandId, EffectDuration duration) {
            this(targetLandId, duration, false, List.of());
        }
    }

    /**
     * Choosing the land type for a snow landwalk grant: the target creature can't be blocked as
     * long as the defending player controls a land that is both snow and of the chosen type
     * (CR 702.14c), until end of turn. Barbarian Guides.
     *
     * @param targetId the target creature that gains snow landwalk of the chosen type
     */
    record SnowLandwalkGrantChoice(UUID targetId) implements ChoiceContext {
    }

    /**
     * The controller has chosen a basic land type; each land they control becomes that type
     * until end of turn, replacing its other land types/mana ability per rule 305.7
     * (Elsewhere Flask).
     *
     * @param controllerId the player whose lands become the chosen type
     */
    record OwnLandsBecomeBasicTypeChoice(UUID controllerId) implements ChoiceContext {
    }

    /**
     * Vision Charm land mode: choose a land type, then a basic land type; each land of the first
     * type becomes the second until end of turn (rule 305.7). When {@code fromType} is null this
     * is the first pick; after answering, a second pick is begun with {@code fromType} set.
     *
     * @param controllerId the resolving controller making both picks
     * @param fromType     null while choosing the land type; the chosen land type while choosing
     *                     the destination basic land type
     */
    record LandsOfTypeBecomeBasicTypeChoice(UUID controllerId, CardSubtype fromType) implements ChoiceContext {
        public LandsOfTypeBecomeBasicTypeChoice(UUID controllerId) {
            this(controllerId, null);
        }
    }

    /**
     * Tracks the sequential "each player names a card" flow for Conundrum Sphinx etc.
     * Players name in APNAP order. After all have named, top cards are revealed and
     * moved to hand (match) or bottom of library (no match).
     *
     * @param playerOrder  all player IDs in APNAP order
     * @param chosenNames  names chosen so far (playerId → chosen name)
     */
    record EachPlayerCardNameRevealChoice(List<UUID> playerOrder,
                                          Map<UUID, String> chosenNames) implements ChoiceContext {}

    /**
     * Sphinx Ambassador: the damaged player names a card after the controller has selected
     * a card from their library. The selected card is stored in
     * the queued {@code PendingSphinxAmbassadorChoice} interaction.
     */
    record SphinxAmbassadorNameChoice(UUID namingPlayerId, UUID controllerId) implements ChoiceContext {}

    /** The damaged player guesses the mana-value range of a card chosen from the controller's hand. */
    record MasterOfPredicamentsGuessChoice(UUID controllerId, Card sourceCard, Card selectedCard)
            implements ChoiceContext {}

    /**
     * Lammastide Weave: the controller names a card, then the target player mills one card. If the
     * milled card matches the chosen name, the controller gains life equal to its mana value.
     */
    record NameCardMillGainLifeChoice(UUID controllerId, UUID targetPlayerId) implements ChoiceContext {}

    /**
     * Foreshadow: the controller names a card, then the target player mills one card. If the milled
     * card matches the chosen name, the controller draws a card.
     */
    record NameCardMillDrawChoice(UUID controllerId, UUID targetPlayerId) implements ChoiceContext {}

    /**
     * Tunnel Vision: the controller names a card, then the target player reveals until finding it.
     * If found, the other revealed cards go to the graveyard and the named card returns to the top;
     * otherwise the target player shuffles their library.
     */
    record ChooseNameRevealUntilNamedPutOnTopRestToGraveyardChoice(UUID controllerId,
                                                                    UUID targetPlayerId)
            implements ChoiceContext {}

    /**
     * The controller names a card, then exiles the top {@code topExileCount} cards of their library
     * and reveals until finding the named card (to hand; rest of the dig exiled). If the named card
     * is never revealed, the entire remaining library is exiled. The controller loses
     * {@code lifeLossPerExiled} life per card exiled by the effect.
     */
    record ChooseNameExileTopRevealUntilNamedChoice(UUID controllerId, int topExileCount,
                                                     int lifeLossPerExiled)
            implements ChoiceContext {}

    /**
     * Wood Sage: the controller names a creature card, then reveals the top {@code count} cards of
     * their library, putting every revealed card with that name into their hand and the rest into
     * their graveyard.
     */
    record ChooseCreatureNameRevealTopCardsChoice(UUID controllerId, Card sourceCard, int count)
            implements ChoiceContext {}

    /**
     * Tamiyo, Collector of Tales: the controller names a nonland card, then reveals the top
     * {@code count} cards of their library, putting matching cards into their hand and the rest
     * into their graveyard.
     */
    record ChooseNonlandCardNameRevealTopCardsChoice(UUID controllerId, Card sourceCard, int count)
            implements ChoiceContext {}

    /**
     * Desperate Research: the controller names a non-basic-land card, then reveals the top
     * {@code count} cards of their library, putting every revealed card with that name into their
     * hand and exiling the rest.
     */
    record ChooseNameRevealTopCardsToHandRestToExileChoice(UUID controllerId, Card sourceCard, int count)
            implements ChoiceContext {}

    /**
     * Comply / Academic Probation: the controller names a card; until their next turn, their
     * opponents can't cast spells with that name. Stamped on
     * {@code GameData.opponentsCantCastNamedSpellsUntilControllerNextTurn}.
     */
    record OpponentsCantCastNamedSpellsUntilNextTurnChoice(UUID controllerId,
                                                           boolean restrictToAllowedNames)
            implements ChoiceContext {

        public OpponentsCantCastNamedSpellsUntilNextTurnChoice(UUID controllerId) {
            this(controllerId, false);
        }
    }

    /**
     * The target player names a card, then reveals the top card of their library. If it matches
     * the named card it goes to their hand; otherwise it goes to their graveyard and the source
     * ({@code sourcePermanentId}) deals {@code damageOnMiss} damage to them ({@code 0} for no
     * damage). Used by Vexing Arcanix.
     */
    record TargetPlayerNameCardRevealTopChoice(UUID controllerId, UUID targetPlayerId, UUID sourcePermanentId,
                                               int damageOnMiss) implements ChoiceContext {}

    /**
     * Diviner's Lockbox: the controller names a card, then reveals the top card of their library.
     * A matching card inserts the source sacrifice and three-card draw into the paused ability.
     */
    record ChooseCardNameRevealTopCardChoice(UUID controllerId) implements ChoiceContext {}

    record ChooseCardNameForDelayedCreatureCombatDamageChoice(
            UUID controllerId,
            List<CardEffect> effects,
            Card sourceCard,
            boolean combatDamageToPlayerOnly,
            boolean untilEndOfTurn
    ) implements ChoiceContext {

        public ChooseCardNameForDelayedCreatureCombatDamageChoice {
            effects = List.copyOf(effects);
        }
    }

    /**
     * Cursed Scroll: the controller names a card, then reveals a card at random from their own hand.
     * If the revealed card has that name, {@code sourceCard} deals {@code damage} damage to
     * {@code targetId} (the any-target chosen when the ability was activated).
     */
    record ChooseNameRevealRandomHandCardDamageChoice(UUID controllerId, UUID targetId, UUID sourcePermanentId,
                                                     Card sourceCard, int damage) implements ChoiceContext {}

    /**
     * Nebuchadnezzar: the controller names a card, then the target player reveals cards at random
     * from their hand and discards the revealed cards with the chosen name.
     */
    record ChooseNameRevealRandomHandCardsDiscardChoice(UUID controllerId, UUID targetPlayerId,
                                                        Card sourceCard, int revealCount)
            implements ChoiceContext {}

    /** The controller names a card; the target reveals their hand and discards matching cards. */
    record ChooseNameRevealHandDiscardChoice(UUID controllerId, UUID targetPlayerId)
            implements ChoiceContext {}

    /**
     * The controller chooses a permanent type at resolution time (e.g. Creeping Renaissance),
     * then all cards of that type are returned from the controller's graveyard.
     */
    record PermanentTypeChoice(UUID controllerId, GraveyardChoiceDestination destination,
                               String entryDescription) implements ChoiceContext {}

    /**
     * Tracks a "choose a mana color, add N of it" choice for effects like Grand Warlord Radha.
     * Also sets mana drain prevention for the controller until end of turn.
     *
     * @param playerId       the player who chooses and receives the mana
     * @param attackerCount  the number of attacking creatures (amount of mana to add)
     */
    record AttackManaSplitChoice(UUID playerId, int attackerCount) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then {@code targetPlayerId} reveals their hand
     * and discards every card of that color (Persecute).
     */
    record DiscardChosenColorChoice(UUID controllerId, UUID targetPlayerId) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then chooses one card of that color from the
     * target player's revealed hand for that player to discard.
     */
    record ChooseColorThenDiscardFromTargetHandChoice(UUID controllerId, UUID targetPlayerId)
            implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then every permanent of that color returns to
     * its owner's hand (Wash Out).
     */
    record ReturnAllPermanentsOfChosenColorChoice(UUID controllerId, PermanentPredicate filter)
            implements ChoiceContext {

        public ReturnAllPermanentsOfChosenColorChoice(UUID controllerId) {
            this(controllerId, null);
        }
    }

    /** The controller chooses a color at resolution, then destroys every matching permanent. */
    record DestroyAllPermanentsOfChosenColorChoice(UUID controllerId, PermanentPredicate filter)
            implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution; {@code targetPlayerId} then exiles the top
     * {@code count} cards of their library and, for each exiled card of the chosen color, the
     * controller creates a token from {@code tokenTemplate} (Oona, Queen of the Fae).
     *
     * @param controllerId   controller that chooses the color and creates the tokens
     * @param targetPlayerId the target opponent who exiles cards
     * @param count          number of top cards to exile
     * @param tokenTemplate  one token created per exiled card of the chosen color
     * @param sourceSetCode  set code of the source card (token art/set)
     */
    record ExileTopCardsChosenColorTokensChoice(UUID controllerId, UUID targetPlayerId, int count,
                                                com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                                String sourceSetCode) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then creates one token from {@code tokenTemplate}
     * for each permanent of that color on the battlefield (any controller; lands excluded, mirroring
     * Oona). Rith, the Awakener.
     *
     * @param controllerId  controller that chooses the color and creates the tokens
     * @param tokenTemplate one token created per permanent of the chosen color
     * @param sourceSetCode set code of the source card (token art/set)
     */
    record CreateTokensPerPermanentOfChosenColorChoice(UUID controllerId,
                                                       com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                                       String sourceSetCode) implements ChoiceContext {}

    /** The controller chooses a color at resolution, then gains one life per matching permanent. */
    record GainLifePerPermanentOfChosenColorChoice(UUID controllerId, Card sourceCard,
                                                   StackEntryType sourceEntryType) implements ChoiceContext {}

    /**
     * Hall of Gemstone: {@code playerId} (the player whose upkeep it is) chooses a color; until end
     * of turn every land tapped for mana produces that color instead of any other color.
     */
    record AllLandsProduceChosenColorChoice(UUID playerId) implements ChoiceContext {}

    /** The controller is choosing the two distinct colors stored by Tablet of the Guilds. */
    record ChooseTwoColorsOnEnterChoice(UUID permanentId, UUID etbTargetId,
                                        List<CardColor> chosen) implements ChoiceContext {}

    /**
     * Storage Matrix: during {@code playerId}'s untap step the active player chooses artifact,
     * creature, or land; only permanents of the chosen type untap this step.
     */
    record StorageMatrixUntapChoice(UUID playerId) implements ChoiceContext {}

    /** Turnabout: choose whether to tap or untap artifacts, creatures, or lands. */
    record TurnaboutChoice(UUID playerId) implements ChoiceContext {}

    /**
     * Teferi's Realm: {@code playerId} (the player whose upkeep it is) chooses artifact, creature,
     * land, or non-Aura enchantment; all nontoken permanents of that type then phase out.
     *
     * @param playerId   the choosing (active) player
     * @param sourceCard the Teferi's Realm permanent's card (for logging / phase-out source)
     */
    record TeferisRealmTypeChoice(UUID playerId, Card sourceCard) implements ChoiceContext {}

    /**
     * Controller chooses one or more colors for {@code targetId}, which then becomes those colors
     * for {@code duration} (until end of turn for Prismwake Merrow / Scuttlemutt; indefinitely for
     * Shyft). Colors are picked one at a time (with a "DONE" option once at least one is chosen);
     * {@code chosen} accumulates the picks so far.
     *
     * @param targetId       the permanent that becomes the chosen colors
     * @param sourceCardName name of the card whose ability created the effect (for display)
     * @param chosen         the colors picked so far
     * @param duration       how long the color set lasts
     */
    record BecomeChosenColorsChoice(UUID targetId, String sourceCardName,
                                    List<CardColor> chosen,
                                    EffectDuration duration) implements ChoiceContext {}

    /**
     * Relic Bind's "choose one" mode pick, made as the enchanted-artifact-tap triggered ability
     * resolves. {@code sourceCard} is the Aura (used for the follow-up target choice and logging);
     * {@code controllerId} chooses the mode and then the target. The two options are
     * {@link #DAMAGE} (deal 1 damage to target player or planeswalker) and {@link #LIFE}
     * (target player gains 1 life).
     */
    record RelicBindModeChoice(Card sourceCard, UUID controllerId) implements ChoiceContext {

        public static final String DAMAGE = "Deal 1 damage to target player or planeswalker";
        public static final String LIFE = "Target player gains 1 life";
        public static final List<String> OPTIONS = List.of(DAMAGE, LIFE);
    }

    /**
     * Hullbreaker Horror's "choose up to one" mode pick, made as the spell-cast triggered ability
     * resolves. {@code sourceCard} is Hullbreaker Horror; {@code controllerId} chooses the mode and
     * then (for SPELL / PERMANENT) the target. {@link #NONE} skips both modes.
     */
    record HullbreakerHorrorModeChoice(Card sourceCard, UUID controllerId) implements ChoiceContext {

        public static final String SPELL = "Return target spell you don't control to its owner's hand";
        public static final String PERMANENT = "Return target nonland permanent to its owner's hand";
        public static final String NONE = "Do nothing";
    }

    /**
     * Quarry Hauler: "for each kind of counter on target permanent, put another counter of that kind
     * on it or remove one from it." The controller answers {@link #ADD}/{@link #REMOVE} once for the
     * first entry of {@code remainingKinds}; the answer is applied to {@code targetId} and, if any
     * kinds are left, the choice re-prompts for the next one until every kind has been resolved.
     *
     * @param targetId       the permanent whose counters are being adjusted
     * @param controllerId   the player making the add/remove decisions
     * @param sourceCardName name of the source card (for the prompt/log)
     * @param remainingKinds the counter kinds still awaiting a decision (first is the current one)
     */
    record AdjustCounterKindChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                   List<CounterType> remainingKinds) implements ChoiceContext {

        public static final String ADD = "ADD";
        public static final String REMOVE = "REMOVE";
        public static final List<String> OPTIONS = List.of(ADD, REMOVE);
    }

    /** Price of Betrayal's per-counter-kind removal choice. */
    record RemoveCountersOfKindChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                      List<String> counterKinds, int index, int remaining)
            implements ChoiceContext {

        public RemoveCountersOfKindChoice {
            counterKinds = List.copyOf(counterKinds);
        }

        public static String counterLabel(String counterKind) {
            return switch (counterKind) {
                case "POISON" -> "poison";
                case "ENERGY" -> "energy";
                case "PLUS_ONE_PLUS_ONE" -> "+1/+1";
                case "MINUS_ONE_MINUS_ONE" -> "-1/-1";
                default -> counterKind.toLowerCase().replace('_', ' ');
            };
        }
    }

    /** Clockspinning's choice of a counter on a permanent or suspended card. */
    record AdjustChosenCounterTypeChoice(UUID targetId, Zone targetZone, UUID controllerId,
                                         String sourceCardName, List<CounterType> counterTypes)
            implements ChoiceContext {

        public AdjustChosenCounterTypeChoice {
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            return counterTypes.stream().map(AdjustChosenCounterTypeChoice::counterLabel).toList();
        }

        public static String counterLabel(CounterType counterType) {
            return switch (counterType) {
                case PLUS_ONE_PLUS_ONE -> "+1/+1 counters";
                case MINUS_ONE_MINUS_ONE -> "-1/-1 counters";
                default -> counterType.name().toLowerCase().replace('_', ' ') + " counters";
            };
        }
    }

    /** Clockspinning's choice to add or remove the selected counter. */
    record AdjustChosenCounterActionChoice(UUID targetId, Zone targetZone, UUID controllerId,
                                           String sourceCardName, CounterType counterType)
            implements ChoiceContext {

        public static final String ADD = "ADD";
        public static final String REMOVE = "REMOVE";
        public static final List<String> OPTIONS = List.of(ADD, REMOVE);
    }

    /** Animation Module's choice of a counter kind to add to the target. */
    record AddAnotherCounterTypeChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                       List<CounterType> counterTypes, boolean poisonCounters)
            implements ChoiceContext {

        public static final String POISON = "poison counters";

        public List<String> options() {
            if (poisonCounters) {
                return List.of(POISON);
            }
            return counterTypes.stream().map(AddAnotherCounterTypeChoice::counterLabel).toList();
        }

        public static String counterLabel(CounterType counterType) {
            return switch (counterType) {
                case PLUS_ONE_PLUS_ONE -> "+1/+1 counters";
                case MINUS_ONE_MINUS_ONE -> "-1/-1 counters";
                default -> counterType.name().toLowerCase().replace('_', ' ') + " counters";
            };
        }
    }

    record RemoveChosenCountersChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                      int remainingSelections, List<CounterType> counterTypes)
            implements ChoiceContext {

        public static final String DONE = "Done";

        public RemoveChosenCountersChoice {
            counterTypes = List.copyOf(counterTypes);
        }

        public List<String> options() {
            List<String> options = new java.util.ArrayList<>(counterTypes.stream()
                    .map(RemoveChosenCountersChoice::counterLabel)
                    .toList());
            options.add(DONE);
            return List.copyOf(options);
        }

        public static String counterLabel(CounterType counterType) {
            return switch (counterType) {
                case PLUS_ONE_PLUS_ONE -> "+1/+1 counters";
                case MINUS_ONE_MINUS_ONE -> "-1/-1 counters";
                default -> counterType.name().toLowerCase().replace('_', ' ') + " counters";
            };
        }
    }

    /** Dismantle's choice of counter type for the destroyed artifact's counters. */
    record DismantleCounterTypeChoice(int counterCount, String sourceCardName) implements ChoiceContext {

        public static final String PLUS_ONE_PLUS_ONE = "+1/+1 counters";
        public static final String CHARGE = "charge counters";
        public static final List<String> OPTIONS = List.of(PLUS_ONE_PLUS_ONE, CHARGE);
    }

    /**
     * A modal triggered ability's "choose one" mode pick, made as the ability resolves (the engine
     * has no cast-time modal machinery for triggered abilities). {@code sourceCard} is the ability's
     * source (used for logging), {@code controllerId} chooses the mode, and {@code effect} carries the
     * {@link ChooseOneEffect}'s options; the chosen mode's effects are spliced into the paused
     * resolution. Used by non-targeting modal upkeep triggers such as Etherwrought Page.
     *
     * <p>A "choose one that hasn't been chosen" trigger (Demonic Pact) instead picks its mode as the
     * ability goes on the stack: {@code triggerTime} is true, {@code sourcePermanentId} identifies the
     * permanent whose consumed modes are recorded, and the chosen mode's effects become their own
     * triggered ability (with that mode's targets) rather than being spliced into a resolution.
     * A targeted modal trigger also sets {@code triggerTime} without setting {@code consumeMode}.
     * {@code asEnters} records a named mode on a permanent before its ETB abilities are collected.
     */
    record ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                            boolean triggerTime, UUID sourcePermanentId, boolean consumeMode,
                            List<String> chosenLabels, boolean asEnters) implements ChoiceContext {

        public ChooseModeChoice {
            chosenLabels = chosenLabels == null ? List.of() : List.copyOf(chosenLabels);
        }

        public ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect) {
            this(sourceCard, controllerId, effect, false, null, false, List.of(), false);
        }

        public ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                boolean triggerTime, UUID sourcePermanentId, boolean consumeMode) {
            this(sourceCard, controllerId, effect, triggerTime, sourcePermanentId, consumeMode, List.of(), false);
        }

        public ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                boolean triggerTime, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effect, triggerTime, sourcePermanentId, false, List.of(), false);
        }

        public ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                UUID sourcePermanentId, boolean asEnters) {
            this(sourceCard, controllerId, effect, false, sourcePermanentId, false, List.of(), asEnters);
        }
    }

    record LibraryCastModeChoice(Card cardToCast, UUID controllerId, ChooseOneEffect effect,
                                 StackEntryType spellType, List<Integer> modeIndices,
                                 Integer discoverValue) implements ChoiceContext {

        public LibraryCastModeChoice {
            modeIndices = List.copyOf(modeIndices);
        }

        public LibraryCastModeChoice(Card cardToCast, UUID controllerId, ChooseOneEffect effect,
                                     StackEntryType spellType, List<Integer> modeIndices) {
            this(cardToCast, controllerId, effect, spellType, modeIndices, null);
        }
    }

    record TriggeredModalChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                UUID sourcePermanentId, boolean modesResetEachTurn,
                                List<ChooseOneEffect.ChooseOneOption> chosenModes,
                                UUID triggeringCardId) implements ChoiceContext {

        public TriggeredModalChoice {
            chosenModes = List.copyOf(chosenModes);
        }

        public TriggeredModalChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                    UUID sourcePermanentId) {
            this(sourceCard, controllerId, effect, sourcePermanentId, false, List.of(), null);
        }

        public TriggeredModalChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                    UUID sourcePermanentId, boolean modesResetEachTurn) {
            this(sourceCard, controllerId, effect, sourcePermanentId, modesResetEachTurn, List.of(), null);
        }
    }

    record RedistributePlayerLifeTotalsChoice(Map<String, Map<UUID, Integer>> choices) implements ChoiceContext {

        public RedistributePlayerLifeTotalsChoice {
            Map<String, Map<UUID, Integer>> copy = new java.util.LinkedHashMap<>();
            choices.forEach((label, totals) -> copy.put(label, Map.copyOf(totals)));
            choices = java.util.Collections.unmodifiableMap(copy);
        }
    }

    /**
     * Torment of Hailfire: {@code affectedPlayerId} (an opponent) chooses one of "sacrifice a nonland
     * permanent", "discard a card", or "lose N life" for one iteration of the effect. The offered
     * options are pruned to what the player can actually do (life is always offered); the "lose life"
     * label is dynamic ({@code "Lose N life"}), so a chosen value that is neither {@link #SACRIFICE}
     * nor {@link #DISCARD} means the life-loss outcome. Answered via {@code handleListChoice}.
     */
    record TormentPenaltyChoice(UUID affectedPlayerId, String sourceCardName) implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a nonland permanent";
        public static final String DISCARD = "Discard a card";
    }

    /**
     * Forbidden Ritual: the targeted opponent chooses one of "sacrifice a permanent", "discard a
     * card", or "lose N life" for one cycle. Offered options are pruned to what the player can do
     * (life is always offered); lands are legal sacrifices (unlike {@link TormentPenaltyChoice}).
     * The "lose life" label is dynamic ({@code "Lose N life"}), so a chosen value that is neither
     * {@link #SACRIFICE} nor {@link #DISCARD} means the life-loss outcome. Answered via
     * {@code handleListChoice}.
     */
    record ForbiddenRitualPenaltyChoice(UUID affectedPlayerId, String sourceCardName) implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a permanent";
        public static final String DISCARD = "Discard a card";
    }

    /**
     * Oath of Lim-Dûl: for each 1 life lost, the controller chooses to sacrifice a permanent other
     * than the source enchantment, or discard a card. Offered options are pruned to what they can
     * do; if neither is possible the iteration is skipped (no prompt). Answered via
     * {@code handleListChoice}.
     */
    record OathOfLimDulPenaltyChoice(UUID affectedPlayerId, String sourceCardName) implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a permanent other than this enchantment";
        public static final String DISCARD = "Discard a card";
    }

    /**
     * Each-player discard-or-sacrifice effect: the affected player chooses to sacrifice a permanent
     * or discard a card. Offered options are pruned to what they can do.
     */
    record EachPlayerSacrificeOrDiscardChoice(UUID affectedPlayerId, String sourceCardName)
            implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a permanent";
        public static final String DISCARD = "Discard a card";
    }

    /**
     * Winter's Chill: the controller of {@code targetPermanentId} chooses pay {2}, pay {1}, or pay
     * nothing for that attacking creature. Offered options are pruned to what the player can
     * afford from their mana pool; "Pay nothing" is always offered. Answered via
     * {@code handleListChoice}.
     */
    record WintersChillPaymentChoice(UUID affectedPlayerId, UUID targetPermanentId, String sourceCardName)
            implements ChoiceContext {

        public static final String PAY_TWO = "Pay {2}";
        public static final String PAY_ONE = "Pay {1}";
        public static final String PAY_NOTHING = "Pay nothing";
    }

    /**
     * Forgotten Lore and Shrouded Lore: after the targeted opponent has chosen a card in the
     * controller's graveyard, the controller chooses whether to pay the configured mana cost and
     * repeat the process. Only offered when the controller can afford it and at least one unchosen
     * card remains. Answered via {@code handleListChoice}.
     */
    record ForgottenLorePaymentChoice(UUID affectedPlayerId, String sourceCardName, String repeatManaCost)
            implements ChoiceContext {

        public static final String PAY = "Pay {G}";
        public static final String DECLINE = "Don't pay";

        public static String payOption(String repeatManaCost) {
            return "Pay " + repeatManaCost;
        }
    }

    /** Indulgent Tormentor: the targeted opponent chooses sacrifice, payment, or a draw. */
    record IndulgentTormentorChoice(UUID affectedPlayerId, int lifeCost, String sourceCardName)
            implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a creature";
        public static final String DRAW = "Draw a card";

        public static String payLife(int lifeCost) {
            return "Pay " + lifeCost + " life";
        }
    }
}
