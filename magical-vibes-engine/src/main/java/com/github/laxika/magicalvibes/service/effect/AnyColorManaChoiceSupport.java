package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;

import java.util.List;
import java.util.UUID;

/**
 * Opens the colour prompt an {@link AwardAnyColorManaEffect} needs and registers whatever rider its
 * {@link ManaSpendRestriction} carries. Shared by the mana-ability path in
 * {@code ActivatedAbilityExecutionService} and the stack handler, so the spend restrictions are
 * expressed once instead of once per call site.
 */
public final class AnyColorManaChoiceSupport {

    private static final List<String> COLORS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");

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
        if (amount <= 0) {
            return false;
        }
        ChoiceContext.ManaColorChoice choiceContext =
                choiceContext(playerId, effect, amount, fromCreature, chosenSubtype);
        if (choiceContext == null) {
            return false;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, COLORS, prompt(effect.restriction())));
        if (effect.restriction() == ManaSpendRestriction.INSTANT_SORCERY_COPY) {
            // Delayed trigger: copy the next instant/sorcery spell this mana is spent on.
            gameData.pendingNextInstantSorceryCopyCount.merge(playerId, 1, Integer::sum);
        }
        return true;
    }

    private static ChoiceContext.ManaColorChoice choiceContext(UUID playerId,
                                                               AwardAnyColorManaEffect effect,
                                                               int amount,
                                                               boolean fromCreature,
                                                               CardSubtype chosenSubtype) {
        return switch (effect.restriction()) {
            case NONE, INSTANT_SORCERY_COPY ->
                    new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount);
            case INSTANT_SORCERY_ONLY -> ChoiceContext.ManaColorChoice.instantSorceryOnly(playerId, amount);
            case FLASHBACK_ONLY ->
                    new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount, null, true);
            case CREATURE_SPELL_ONLY -> ChoiceContext.ManaColorChoice.creatureSpellOnly(playerId, amount);
            case SUBTYPE_SPELL ->
                    new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount, effect.subtype());
            case CHOSEN_SUBTYPE_CREATURE -> chosenSubtype == null
                    ? null
                    : new ChoiceContext.ManaColorChoice(playerId, fromCreature, amount, chosenSubtype);
            case CHOSEN_SUBTYPE_CREATURE_UNCOUNTERABLE -> chosenSubtype == null
                    ? null
                    : ChoiceContext.ManaColorChoice.chosenSubtypeCreatureUncounterable(playerId, amount, chosenSubtype);
            case SUBTYPE_SPELL_OR_ABILITY ->
                    ChoiceContext.ManaColorChoice.subtypeSpellOrAbility(playerId, amount, effect.subtype());
        };
    }

    private static String prompt(ManaSpendRestriction restriction) {
        return switch (restriction) {
            case INSTANT_SORCERY_ONLY -> "Choose a color of mana to add (instant and sorcery spells only).";
            case FLASHBACK_ONLY -> "Choose a color of mana to add (flashback only).";
            default -> "Choose a color of mana to add.";
        };
    }
}
