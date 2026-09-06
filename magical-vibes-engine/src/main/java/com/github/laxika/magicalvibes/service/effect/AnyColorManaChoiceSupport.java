package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Opens the colour prompt an {@link AwardAnyColorManaEffect} needs and registers whatever rider its
 * {@link ManaSpendRestriction} carries. Shared by the mana-ability path in
 * {@code ActivatedAbilityExecutionService} and the stack handler, so the spend restrictions are
 * expressed once instead of once per call site.
 */
public final class AnyColorManaChoiceSupport {

    private AnyColorManaChoiceSupport() {
    }

    /**
     * Prompts {@code playerId} for the colour(s) of {@code amount} mana.
     *
     * @param amount        the already-evaluated, multiplier-applied mana count
     * @param fromCreature  whether the producing source is a creature (creature-mana tracking)
     * @param chosenSubtype the creature type chosen as the source permanent entered, for the
     *                      {@code CHOSEN_SUBTYPE_*} restrictions; {@code null} elsewhere
     * @return {@code false} when nothing was prompted — no mana to add, or a chosen-subtype form
     *         whose source never got a type
     */
    public static boolean beginColorChoice(InteractionHandlerRegistry interactionHandlerRegistry,
                                           GameData gameData,
                                           UUID playerId,
                                           AwardAnyColorManaEffect effect,
                                           int amount,
                                           boolean fromCreature,
                                           CardSubtype chosenSubtype) {
        return beginColorChoice(interactionHandlerRegistry, gameData, playerId, effect, amount,
                fromCreature, chosenSubtype, null, null);
    }

    public static boolean beginColorChoice(InteractionHandlerRegistry interactionHandlerRegistry,
                                           GameData gameData,
                                           UUID playerId,
                                           AwardAnyColorManaEffect effect,
                                           int amount,
                                           boolean fromCreature,
                                           CardSubtype chosenSubtype,
                                           Card sourceCard) {
        return beginColorChoice(interactionHandlerRegistry, gameData, playerId, effect, amount,
                fromCreature, chosenSubtype, sourceCard, null);
    }

    public static boolean beginColorChoice(InteractionHandlerRegistry interactionHandlerRegistry,
                                           GameData gameData,
                                           UUID playerId,
                                           AwardAnyColorManaEffect effect,
                                           int amount,
                                           boolean fromCreature,
                                           CardSubtype chosenSubtype,
                                           Card sourceCard,
                                           UUID sourcePermanentId) {
        return beginColorChoice(interactionHandlerRegistry, gameData, playerId, effect, amount,
                fromCreature, chosenSubtype, sourceCard, sourcePermanentId, null);
    }

    public static boolean beginColorChoice(InteractionHandlerRegistry interactionHandlerRegistry,
                                           GameData gameData,
                                           UUID playerId,
                                           AwardAnyColorManaEffect effect,
                                           int amount,
                                           boolean fromCreature,
                                           CardSubtype chosenSubtype,
                                           Card sourceCard,
                                           UUID sourcePermanentId,
                                           UUID recipientPlayerId) {
        return beginColorChoice(interactionHandlerRegistry, gameData, playerId, effect, amount,
                fromCreature, chosenSubtype, sourceCard, sourcePermanentId, recipientPlayerId, false);
    }

    public static boolean beginColorChoice(InteractionHandlerRegistry interactionHandlerRegistry,
                                           GameData gameData,
                                           UUID playerId,
                                           AwardAnyColorManaEffect effect,
                                           int amount,
                                           boolean fromCreature,
                                           CardSubtype chosenSubtype,
                                           Card sourceCard,
                                           UUID sourcePermanentId,
                                           UUID recipientPlayerId,
                                           boolean fromSnowSource) {
        return beginColorChoice(interactionHandlerRegistry, gameData, playerId, effect, amount,
                fromCreature, chosenSubtype, sourceCard, sourcePermanentId, recipientPlayerId,
                fromSnowSource, false, null);
    }

    public static boolean beginColorChoice(InteractionHandlerRegistry interactionHandlerRegistry,
                                           GameData gameData,
                                           UUID playerId,
                                           AwardAnyColorManaEffect effect,
                                           int amount,
                                           boolean fromCreature,
                                           CardSubtype chosenSubtype,
                                           Card sourceCard,
                                           UUID sourcePermanentId,
                                            UUID recipientPlayerId,
                                            boolean fromSnowSource,
                                            Set<CardColor> sourceColors) {
        return beginColorChoice(interactionHandlerRegistry, gameData, playerId, effect, amount,
                fromCreature, chosenSubtype, sourceCard, sourcePermanentId, recipientPlayerId,
                fromSnowSource, false, sourceColors);
    }

    public static boolean beginColorChoice(InteractionHandlerRegistry interactionHandlerRegistry,
                                           GameData gameData, UUID playerId,
                                           AwardAnyColorManaEffect effect, int amount,
                                           boolean fromCreature, CardSubtype chosenSubtype,
                                           Card sourceCard, UUID sourcePermanentId,
                                           UUID recipientPlayerId, boolean fromSnowSource,
                                           boolean fromCaveSource) {
        return beginColorChoice(interactionHandlerRegistry, gameData, playerId, effect, amount,
                fromCreature, chosenSubtype, sourceCard, sourcePermanentId, recipientPlayerId,
                fromSnowSource, fromCaveSource, null);
    }

    public static boolean beginColorChoice(InteractionHandlerRegistry interactionHandlerRegistry,
                                           GameData gameData, UUID playerId,
                                           AwardAnyColorManaEffect effect, int amount,
                                           boolean fromCreature, CardSubtype chosenSubtype,
                                           Card sourceCard, UUID sourcePermanentId,
                                           UUID recipientPlayerId, boolean fromSnowSource,
                                           boolean fromCaveSource, Set<CardColor> sourceColors) {
        if (amount <= 0) {
            return false;
        }
        ChoiceContext choiceContext =
                choiceContext(gameData, playerId, effect, amount, fromCreature, chosenSubtype,
                        sourceCard, sourcePermanentId, sourceColors);
        if (choiceContext == null) {
            return false;
        }
        if (effect.sourceBecomesProducedColorUntilEndOfTurn()) {
            if (choiceContext instanceof ChoiceContext.ManaColorChoice manaColorChoice) {
                choiceContext = manaColorChoice.withSourcePermanentId(sourcePermanentId);
            }
        }
        if (recipientPlayerId != null) {
            if (choiceContext instanceof ChoiceContext.ManaColorChoice manaColorChoice) {
                choiceContext = manaColorChoice.withRecipientPlayerId(recipientPlayerId);
            } else if (choiceContext instanceof ChoiceContext.SpellOnlyManaColorChoice spellOnlyChoice) {
                choiceContext = spellOnlyChoice.withRecipientPlayerId(recipientPlayerId);
            } else if (choiceContext instanceof ChoiceContext.MulticoloredSpellManaColorChoice multicoloredChoice) {
                choiceContext = multicoloredChoice.withRecipientPlayerId(recipientPlayerId);
            }
        }
        if (fromSnowSource && choiceContext instanceof ChoiceContext.ManaColorChoice manaColorChoice) {
            choiceContext = manaColorChoice.withSnowSource(true);
        } else if (fromSnowSource
                && choiceContext instanceof ChoiceContext.MulticoloredSpellManaColorChoice multicoloredChoice) {
            choiceContext = multicoloredChoice.withSnowSource(true);
        }
        if (fromCaveSource && choiceContext instanceof ChoiceContext.ManaColorChoice manaColorChoice) {
            choiceContext = manaColorChoice.withCaveSource(true);
        } else if (fromCaveSource
                && choiceContext instanceof ChoiceContext.MulticoloredSpellManaColorChoice multicoloredChoice) {
            choiceContext = multicoloredChoice.withCaveSource(true);
        }
        List<ManaColor> allowedColors = switch (effect.restriction()) {
            case IMPRINTED_CARD_COLORS -> imprintedCardColors(gameData, sourceCard);
            case EXILED_CARD_COLORS -> exiledCardColors(gameData, sourcePermanentId);
            case SOURCE_PERMANENT_COLORS, CREATURE_COLORS_ABILITIES -> sourcePermanentColors(sourceColors);
            default -> effect.allowedColors();
        };
        if (allowedColors.size() == 1
                && (effect.restriction() == ManaSpendRestriction.IMPRINTED_CARD_COLORS
                || effect.restriction() == ManaSpendRestriction.EXILED_CARD_COLORS
                || effect.restriction() == ManaSpendRestriction.SOURCE_PERMANENT_COLORS
                || effect.restriction() == ManaSpendRestriction.CREATURE_COLORS_ABILITIES)) {
            UUID manaRecipientId = recipientPlayerId != null ? recipientPlayerId : playerId;
            ManaPool manaPool = gameData.playerManaPools.get(manaRecipientId);
            ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, playerId, allowedColors.get(0));
            if (effect.restriction() == ManaSpendRestriction.CREATURE_COLORS_ABILITIES) {
                manaPool.addCreatureAbilityOnlyMana(effectiveColor, amount);
            } else {
                manaPool.add(effectiveColor, amount);
                if (fromSnowSource) {
                    manaPool.addSnowManaTag(effectiveColor, amount);
                }
                if (fromCaveSource) {
                    manaPool.addCaveManaTag(effectiveColor, amount);
                }
                if (fromCreature) {
                    manaPool.addCreatureMana(effectiveColor, amount);
                }
            }
            return false;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext,
                allowedColors.stream().map(Enum::name).toList(), prompt(effect.restriction())));
        if (effect.restriction() == ManaSpendRestriction.INSTANT_SORCERY_COPY) {
            // Delayed trigger: copy the next instant/sorcery spell this mana is spent on.
            gameData.pendingNextInstantSorceryCopyCount.merge(playerId, 1, Integer::sum);
        }
        return true;
    }

    private static ChoiceContext choiceContext(GameData gameData,
                                               UUID playerId,
                                               AwardAnyColorManaEffect effect,
                                               int amount,
                                               boolean fromCreature,
                                               CardSubtype chosenSubtype,
                                               Card sourceCard,
                                               UUID sourcePermanentId,
                                               Set<CardColor> sourceColors) {
        if (effect.differentColors()) {
            ChoiceContext.ManaColorChoice choice = ChoiceContext.ManaColorChoice.differentColors(
                    playerId, fromCreature, amount, effect.allowedColors());
            if (effect.restriction() == ManaSpendRestriction.PLANESWALKER_SPELLS) {
                choice = choice.withPlaneswalkerSpellOnly();
            }
            return effect.grantsAdditionalPlusOneCounter() ? choice.withAdditionalPlusOneCounter() : choice;
        }
        if (effect.anyColorCombination()) {
            if (effect.restriction() == ManaSpendRestriction.SUBTYPE_SPELL
                    && !effect.spellOnlySubtypes().isEmpty()) {
                return ChoiceContext.ManaColorSpellChoice.anyColorCombination(
                        playerId, amount, effect.spellOnlySubtypes());
            }
            if (effect.restriction() == ManaSpendRestriction.SPELL_ONLY) {
                return new ChoiceContext.SpellOnlyManaColorChoice(
                        playerId, fromCreature, amount, true);
            }
            if (effect.restriction() == ManaSpendRestriction.MULTICOLORED_SPELLS) {
                return new ChoiceContext.MulticoloredSpellManaColorChoice(
                        playerId, fromCreature, amount, true);
            }
            ChoiceContext.ManaColorChoice choice = ChoiceContext.ManaColorChoice.fixedColorCombination(
                    playerId, fromCreature, amount, effect.allowedColors());
            if (effect.restriction() == ManaSpendRestriction.PLANESWALKER_SPELLS) {
                choice = choice.withPlaneswalkerSpellOnly();
            }
            return effect.grantsAdditionalPlusOneCounter() ? choice.withAdditionalPlusOneCounter() : choice;
        }

        ChoiceContext choice = switch (effect.restriction()) {
            case NONE, INSTANT_SORCERY_COPY -> effect.restriction() == ManaSpendRestriction.NONE
                    && sourceCard != null
                    && sourceCard.getSubtypes().contains(CardSubtype.TREASURE)
                    ? new ChoiceContext.TreasureManaColorChoice(playerId, amount)
                    : new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount);
            case SPELL_ONLY ->
                    new ChoiceContext.SpellOnlyManaColorChoice(playerId, fromCreature, amount, false);
            case MULTICOLORED_SPELLS ->
                    new ChoiceContext.MulticoloredSpellManaColorChoice(playerId, fromCreature, amount, false);
            case ABILITIES -> ChoiceContext.ManaColorChoice.abilityOnly(playerId, amount);
            case IMPRINTED_CARD_COLORS -> {
                List<ManaColor> colors = imprintedCardColors(gameData, sourceCard);
                yield colors.isEmpty()
                        ? null
                        : ChoiceContext.ManaColorChoice.fixedColorCombination(
                                playerId, fromCreature, amount, colors);
            }
            case EXILED_CARD_COLORS -> {
                List<ManaColor> colors = exiledCardColors(gameData, sourcePermanentId);
                yield colors.isEmpty()
                        ? null
                        : ChoiceContext.ManaColorChoice.fixedColorCombination(
                                playerId, fromCreature, amount, colors);
            }
            case SOURCE_PERMANENT_COLORS -> {
                List<ManaColor> colors = sourcePermanentColors(sourceColors);
                yield colors.isEmpty()
                        ? null
                        : ChoiceContext.ManaColorChoice.fixedColorCombination(
                                playerId, fromCreature, amount, colors);
            }
            case INSTANT_SORCERY_ONLY -> ChoiceContext.ManaColorChoice.instantSorceryOnly(playerId, amount);
            case ARTIFACT_SPELLS_OR_ABILITIES ->
                    ChoiceContext.ManaColorChoice.artifactSpellOrAbilityOnly(playerId, amount);
            case FLASHBACK_ONLY ->
                    new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount, null, true);
            case EXILED_SPELL_ONLY ->
                    new ChoiceContext.ExiledSpellManaColorChoice(playerId, fromCreature, amount);
            case GRAVEYARD_SPELL_ONLY ->
                    new ChoiceContext.GraveyardManaColorChoice(playerId, fromCreature, amount);
            case CREATURE_SPELL_ONLY -> ChoiceContext.ManaColorChoice.creatureSpellOnly(playerId, fromCreature, amount);
            case CREATURE_OR_ENCHANTMENT_SPELL_ONLY ->
                    throw new IllegalArgumentException("Use the two-color mana effect for this restriction");
            case SUBTYPE_CREATURE_SPELL ->
                    new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount, effect.subtype());
            case CREATURE_SPELLS_OR_ABILITIES ->
                    ChoiceContext.ManaColorChoice.creatureSpellOrAbilityOnly(playerId, amount);
            case CREATURE_COLORS_ABILITIES ->
                    ChoiceContext.ManaColorChoice.creatureAbilityOnly(
                            playerId, fromCreature, amount, sourcePermanentColors(sourceColors));
            case MOUNT_OR_VEHICLE_SPELL ->
                    new ChoiceContext.ManaColorSpellChoice(
                            playerId, amount, Set.of(CardSubtype.MOUNT, CardSubtype.VEHICLE));
            case PLANESWALKER_SPELLS ->
                    ChoiceContext.ManaColorChoice.planeswalkerSpellOnly(playerId, amount);
            case SUBTYPE_SPELL -> effect.spellOnlySubtypes().isEmpty()
                    ? new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount, effect.subtype())
                    : new ChoiceContext.ManaColorSpellChoice(playerId, amount, effect.spellOnlySubtypes());
            case CHOSEN_SUBTYPE_CREATURE -> chosenSubtype == null
                    ? null
                    : new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount, chosenSubtype);
            case CHOSEN_SUBTYPE_CREATURE_UNCOUNTERABLE -> chosenSubtype == null
                    ? null
                    : ChoiceContext.ManaColorChoice.chosenSubtypeCreatureUncounterable(playerId, amount, chosenSubtype);
            case CHOSEN_SUBTYPE_SPELL_OR_ABILITY -> chosenSubtype == null
                    ? null
                    : ChoiceContext.ManaColorChoice.subtypeSpellOrAbility(playerId, amount, chosenSubtype);
            case CHOSEN_SUBTYPE_CREATURE_SOURCE_SPELL_OR_ABILITY -> chosenSubtype == null
                    ? null
                    : ChoiceContext.ManaColorChoice.creatureSourceSpellOrAbility(playerId, amount, chosenSubtype);
            case SUBTYPE_SPELL_OR_ABILITY ->
                    ChoiceContext.ManaColorChoice.subtypeSpellOrAbility(playerId, amount, effect.subtype());
            case MANA_VALUE_AT_LEAST_FOUR ->
                    ChoiceContext.ManaColorChoice.manaValueAtLeastFour(playerId, amount);
            case CREATURE_SPELL_MANA_VALUE_AT_LEAST_FOUR_OR_X ->
                    ChoiceContext.ManaColorChoice.creatureSpellManaValueAtLeastFourOrXOnly(playerId, amount);
            case PARTY_SPELL_OR_ABILITY ->
                    ChoiceContext.ManaColorChoice.partySpellOrAbility(playerId, amount);
        };
        return effect.grantsAdditionalPlusOneCounter() && choice instanceof ChoiceContext.ManaColorChoice manaChoice
                ? manaChoice.withAdditionalPlusOneCounter() : choice;
    }

    private static List<ManaColor> imprintedCardColors(GameData gameData, Card sourceCard) {
        if (sourceCard == null) {
            return List.of();
        }
        Card imprintedCard = gameData.getImprintedCard(sourceCard);
        if (imprintedCard == null || gameData.findExiledCard(imprintedCard.getId()) == null
                || imprintedCard.getColors() == null) {
            return List.of();
        }
        return imprintedCard.getColors().stream()
                .map(CardColor::name)
                .map(ManaColor::valueOf)
                .toList();
    }

    private static List<ManaColor> exiledCardColors(GameData gameData, UUID sourcePermanentId) {
        if (sourcePermanentId == null) {
            return List.of();
        }
        return ManaColor.COLORS.stream()
                .filter(color -> gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                        .anyMatch(card -> card.getColors().contains(CardColor.valueOf(color.name()))))
                .toList();
    }

    private static List<ManaColor> sourcePermanentColors(Set<CardColor> sourceColors) {
        if (sourceColors == null || sourceColors.isEmpty()) {
            return List.of();
        }
        return ManaColor.COLORS.stream()
                .filter(color -> sourceColors.contains(CardColor.valueOf(color.name())))
                .toList();
    }

    private static String prompt(ManaSpendRestriction restriction) {
        return switch (restriction) {
            case SPELL_ONLY -> "Choose a color of mana to add (spells only).";
            case MULTICOLORED_SPELLS -> "Choose a color of mana to add (multicolored spells only).";
            case ABILITIES -> "Choose a color of mana to add (activated abilities only).";
            case EXILED_CARD_COLORS -> "Choose a color among the exiled cards' colors.";
            case INSTANT_SORCERY_ONLY -> "Choose a color of mana to add (instant and sorcery spells only).";
            case ARTIFACT_SPELLS_OR_ABILITIES -> "Choose a color of mana to add (artifact spells or artifact abilities only).";
            case CREATURE_SPELLS_OR_ABILITIES -> "Choose a color of mana to add (creature spells or creature abilities only).";
            case CREATURE_COLORS_ABILITIES -> "Choose a color of mana to add (creature abilities only).";
            case FLASHBACK_ONLY -> "Choose a color of mana to add (flashback only).";
            case EXILED_SPELL_ONLY -> "Choose a color of mana to add (spells from exile only).";
            case GRAVEYARD_SPELL_ONLY -> "Choose a color of mana to add (graveyard spells only).";
            case MANA_VALUE_AT_LEAST_FOUR -> "Choose a color of mana to add (spells with mana value 4 or greater only).";
            case CREATURE_SPELL_MANA_VALUE_AT_LEAST_FOUR_OR_X ->
                    "Choose a color of mana to add (qualifying creature spells only).";
            case SOURCE_PERMANENT_COLORS -> "Choose a color of mana to add from this creature's colors.";
            case PLANESWALKER_SPELLS -> "Choose a color of mana to add (planeswalker spells only).";
            default -> "Choose a color of mana to add.";
        };
    }
}
