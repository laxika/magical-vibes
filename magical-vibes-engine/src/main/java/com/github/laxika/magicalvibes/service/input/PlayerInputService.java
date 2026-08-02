package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.turn.UntapStepService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerInputService {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        beginCardChoice(gameData, playerId, validIndices, prompt, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, boolean enterTapped) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, enterTapped));
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, null);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, false, null, null, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                                String drawAndRepeatLabel) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                                String drawAndRepeatLabel, boolean putAnyNumber) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel,
                putAnyNumber));
    }

    /** Flash-style: choose a creature to put onto the battlefield, then pay its cost reduced by N or sacrifice it. */
    public void beginCardChoiceSacrificeUnlessPayReduced(GameData gameData, UUID playerId, List<Integer> validIndices,
                                                         String prompt, int genericReduction) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, Integer.valueOf(genericReduction)));
    }

    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID targetId) {
        beginTargetedCardChoice(gameData, playerId, validIndices, prompt, targetId, null);
    }

    /**
     * Variant where declining the choice exiles the given permanent (e.g. Evershrike: "You may put an
     * Aura ... onto the battlefield attached to it. If you don't, exile this creature.").
     */
    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                        UUID targetId, UUID exileSourceIfDeclinedId) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TargetedHandCardChoice(
                playerId, new ArrayList<>(validIndices), targetId, prompt, exileSourceIfDeclinedId));
    }

    public void beginPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                playerId, new ArrayList<>(validIds), List.of(),
                gameData.interaction.permanentChoiceContext(), prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent", gameData.id, playerName);
    }

    public void beginAnyTargetChoice(GameData gameData, UUID playerId, List<UUID> validPermanentIds, List<UUID> validPlayerIds, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                playerId, new ArrayList<>(validPermanentIds), new ArrayList<>(validPlayerIds),
                gameData.interaction.permanentChoiceContext(), prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose any target", gameData.id, playerName);
    }

    public void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount, String prompt) {
        beginMultiPermanentChoice(gameData, playerId, validIds, maxCount, null, prompt);
    }

    public void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount,
                                          MultiPermanentChoiceContext context, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiPermanentChoice(
                playerId, new ArrayList<>(validIds), maxCount, context, prompt));
    }

    public void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<Card> cards, int maxCount, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                playerId, new ArrayList<>(cards), maxCount, prompt));
    }

    public void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetId) {
        beginColorChoice(gameData, playerId, permanentId, etbTargetId,
                List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN));
    }

    /**
     * As {@link #beginColorChoice(GameData, UUID, UUID, UUID)} but restricted to {@code allowedColors}
     * — for cards that narrow the pick ("choose black or red", Mangara's Equity).
     */
    public void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetId,
            List<CardColor> allowedColors) {
        List<String> colors = allowedColors.stream().map(Enum::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, permanentId, etbTargetId, null, colors,
                colors.size() < 5 ? "Choose " + String.join(" or ", colors).toLowerCase() + "." : "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    public void beginDiscardChosenColorChoice(GameData gameData, UUID controllerId, UUID targetPlayerId) {
        ChoiceContext.DiscardChosenColorChoice ctx = new ChoiceContext.DiscardChosenColorChoice(controllerId, targetPlayerId);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (discard all cards of that color)", gameData.id, playerName);
    }

    public void beginExileTopCardsChosenColorTokensChoice(GameData gameData, UUID controllerId, UUID targetPlayerId,
            int count, com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate, String sourceSetCode) {
        ChoiceContext.ExileTopCardsChosenColorTokensChoice ctx =
                new ChoiceContext.ExileTopCardsChosenColorTokensChoice(controllerId, targetPlayerId, count, tokenTemplate, sourceSetCode);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (Oona-style exile/token)", gameData.id, playerName);
    }

    public void beginCreateTokensPerPermanentOfChosenColorChoice(GameData gameData, UUID controllerId,
            com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate, String sourceSetCode) {
        ChoiceContext.CreateTokensPerPermanentOfChosenColorChoice ctx =
                new ChoiceContext.CreateTokensPerPermanentOfChosenColorChoice(controllerId, tokenTemplate, sourceSetCode);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (Rith token-per-permanent)", gameData.id, playerName);
    }

    /**
     * Hall of Gemstone: {@code playerId} chooses the color every land produces for the rest of the
     * turn.
     */
    public void beginAllLandsProduceChosenColorChoice(GameData gameData, UUID playerId) {
        ChoiceContext.AllLandsProduceChosenColorChoice ctx =
                new ChoiceContext.AllLandsProduceChosenColorChoice(playerId);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color (all lands produce that color)", gameData.id, playerName);
    }

    public void beginProtectionColorChoice(GameData gameData, UUID playerId, UUID targetId, boolean includeArtifacts) {
        beginProtectionColorChoice(gameData, playerId, List.of(targetId), includeArtifacts);
    }

    /**
     * One protection pick shared by several targets (Prismatic Boon's "X target creatures gain
     * protection from the chosen color").
     */
    public void beginProtectionColorChoice(GameData gameData, UUID playerId, List<UUID> targetIds, boolean includeArtifacts) {
        ChoiceContext.ProtectionColorChoice ctx = new ChoiceContext.ProtectionColorChoice(targetIds, includeArtifacts);

        List<String> options = new java.util.ArrayList<>(List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN"));
        if (includeArtifacts) {
            options.addFirst("ARTIFACT");
        }
        String prompt = includeArtifacts ? "Choose a color or artifacts." : "Choose a color.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose protection", gameData.id, playerName);
    }

    public void beginColorSetChoice(GameData gameData, UUID controllerId, UUID targetId, String sourceCardName) {
        ChoiceContext.ColorSetChoice ctx = new ChoiceContext.ColorSetChoice(targetId, controllerId, sourceCardName);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (target becomes chosen color)", gameData.id, playerName);
    }

    /**
     * Prismwake Merrow: prompt {@code playerId} to pick a color for {@code targetId}. Colors are
     * picked one at a time — already-chosen colors are dropped from the options, and "DONE" is
     * offered once at least one color has been chosen. The choice handler accumulates the picks and
     * re-invokes this until the player is done (see {@code ChoiceHandlerService}).
     */
    public void beginBecomeChosenColorsChoice(GameData gameData, UUID playerId, UUID targetId,
                                              String sourceCardName, List<CardColor> chosen,
                                              EffectDuration duration) {
        ChoiceContext.BecomeChosenColorsChoice ctx =
                new ChoiceContext.BecomeChosenColorsChoice(targetId, sourceCardName, new ArrayList<>(chosen),
                        duration);

        List<String> options = new ArrayList<>();
        for (CardColor color : List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN)) {
            if (!chosen.contains(color)) {
                options.add(color.name());
            }
        }
        if (!chosen.isEmpty()) {
            options.add("DONE");
        }
        String prompt = chosen.isEmpty() ? "Choose a color." : "Choose another color, or DONE.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    public void beginMassProtectionColorChoice(GameData gameData, UUID controllerId) {
        ChoiceContext.MassProtectionColorChoice ctx = new ChoiceContext.MassProtectionColorChoice(controllerId);

        List<String> options = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, options, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose protection (you and your permanents)", gameData.id, playerName);
    }

    public void beginRelicBindModeChoice(GameData gameData, UUID controllerId, Card sourceCard) {
        ChoiceContext.RelicBindModeChoice ctx = new ChoiceContext.RelicBindModeChoice(sourceCard, controllerId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx,
                ChoiceContext.RelicBindModeChoice.OPTIONS, sourceCard.getName() + " — Choose one."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose Relic Bind's mode", gameData.id, playerName);
    }

    public void beginChooseModeChoice(GameData gameData, UUID controllerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect) {
        ChoiceContext.ChooseModeChoice ctx = new ChoiceContext.ChooseModeChoice(sourceCard, controllerId, effect);
        List<String> optionLabels = effect.options().stream()
                .map(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption::label)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, optionLabels, sourceCard.getName() + " — Choose one."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a mode for {}", gameData.id, playerName, sourceCard.getName());
    }

    /**
     * Hullbreaker Horror "choose up to one" — offers bounce-spell / bounce-permanent / do-nothing,
     * omitting a bounce mode when it currently has no legal target.
     */
    public void beginHullbreakerHorrorModeChoice(GameData gameData, UUID controllerId, Card sourceCard) {
        java.util.ArrayList<String> options = new java.util.ArrayList<>();
        if (hasOpponentSpellOnStack(gameData, controllerId)) {
            options.add(ChoiceContext.HullbreakerHorrorModeChoice.SPELL);
        }
        if (hasNonlandPermanent(gameData)) {
            options.add(ChoiceContext.HullbreakerHorrorModeChoice.PERMANENT);
        }
        options.add(ChoiceContext.HullbreakerHorrorModeChoice.NONE);

        ChoiceContext.HullbreakerHorrorModeChoice ctx =
                new ChoiceContext.HullbreakerHorrorModeChoice(sourceCard, controllerId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx,
                options, sourceCard.getName() + " — Choose up to one."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose Hullbreaker Horror's mode", gameData.id, playerName);
    }

    private static boolean hasOpponentSpellOnStack(GameData gameData, UUID controllerId) {
        for (StackEntry se : gameData.stack) {
            StackEntryType type = se.getEntryType();
            if (type == StackEntryType.ACTIVATED_ABILITY || type == StackEntryType.TRIGGERED_ABILITY) {
                continue;
            }
            if (!controllerId.equals(se.getControllerId())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNonlandPermanent(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            java.util.List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!permanent.getCard().hasType(CardType.LAND)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void beginKeywordChoice(GameData gameData, UUID playerId, UUID targetId, List<Keyword> options) {
        ChoiceContext.KeywordGrantChoice choiceContext = new ChoiceContext.KeywordGrantChoice(targetId, options);

        List<String> optionNames = options.stream().map(Keyword::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, optionNames, "Choose a keyword to grant."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a keyword", gameData.id, playerName);
    }

    public void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.SubtypeChoice choiceContext = new ChoiceContext.SubtypeChoice(permanentId);

        List<String> creatureTypes = Arrays.stream(CardSubtype.values())
                .filter(s -> !NON_CREATURE_SUBTYPES.contains(s))
                .map(CardSubtype::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, creatureTypes, "Choose a creature type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a creature type", gameData.id, playerName);
    }

    public void beginSpellCreatureTypeChoice(GameData gameData, UUID playerId) {
        ChoiceContext.SpellCreatureTypeChoice choiceContext = new ChoiceContext.SpellCreatureTypeChoice(playerId);

        List<String> creatureTypes = Arrays.stream(CardSubtype.values())
                .filter(s -> !NON_CREATURE_SUBTYPES.contains(s))
                .map(CardSubtype::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, creatureTypes, "Choose a creature type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a creature type", gameData.id, playerName);
    }

    public void beginManaValueParityChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.ManaValueParityChoice choiceContext = new ChoiceContext.ManaValueParityChoice(permanentId);

        List<String> options = List.of("ODD", "EVEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options, "Choose odd or even."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose odd or even", gameData.id, playerName);
    }

    public void beginNumberChoice(GameData gameData, UUID playerId, UUID permanentId, int min, int max) {
        ChoiceContext.NumberChoice choiceContext = new ChoiceContext.NumberChoice(permanentId);

        List<String> options = java.util.stream.IntStream.rangeClosed(min, max)
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose a number between " + min + " and " + max + "."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a number between {} and {}", gameData.id, playerName, min, max);
    }

    public void beginPayAnyAmountOfLifeChoice(GameData gameData, UUID playerId, int maxLife,
                                              ChoiceContext.PayAnyAmountOfLifeAsEnters choiceContext) {
        List<String> options = java.util.stream.IntStream.rangeClosed(0, Math.max(0, maxLife))
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                choiceContext.card().getName() + " — pay any amount of life (0-" + Math.max(0, maxLife) + ")."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to pay any amount of life for {}", gameData.id, playerName,
                choiceContext.card().getName());
    }

    public void beginTetravusCounterRemovalChoice(GameData gameData, UUID playerId, UUID permanentId,
                                                  int maxCounters,
                                                  com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate) {
        ChoiceContext.TetravusCounterRemoval choiceContext =
                new ChoiceContext.TetravusCounterRemoval(permanentId, tokenTemplate);

        List<String> options = java.util.stream.IntStream.rangeClosed(0, maxCounters)
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Remove any number of +1/+1 counters (0-" + maxCounters
                        + ") to create that many Tetravite tokens."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose how many +1/+1 counters to remove (0-{})",
                gameData.id, playerName, maxCounters);
    }

    /**
     * Quarry Hauler: prompt {@code playerId} to add or remove one counter of the FIRST kind in
     * {@code remainingKinds} on {@code targetId}. {@link ChoiceHandlerService} applies the answer and
     * re-invokes this with the remaining kinds until every kind has been resolved.
     */
    public void beginAdjustCounterKindChoice(GameData gameData, UUID playerId, UUID targetId,
                                             String sourceCardName, List<CounterType> remainingKinds) {
        ChoiceContext.AdjustCounterKindChoice ctx = new ChoiceContext.AdjustCounterKindChoice(
                targetId, playerId, sourceCardName, new ArrayList<>(remainingKinds));

        CounterType current = remainingKinds.getFirst();
        String label = current.name().toLowerCase().replace('_', ' ');
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, ChoiceContext.AdjustCounterKindChoice.OPTIONS,
                sourceCardName + " — Add or remove a " + label + " counter?"));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to add/remove a {} counter", gameData.id, playerName, current);
    }

    public void beginPrimalClayFormChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.PrimalClayFormChoice choiceContext = new ChoiceContext.PrimalClayFormChoice(permanentId);

        List<String> options = Arrays.stream(com.github.laxika.magicalvibes.model.PrimalClayForm.values())
                .map(Enum::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose a shape: 3/3, 2/2 with flying, or 1/6 Wall with defender."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a Primal Clay shape", gameData.id, playerName);
    }

    public void beginPermanentTypeChoice(GameData gameData, UUID playerId, GraveyardChoiceDestination destination, String entryDescription) {
        ChoiceContext.PermanentTypeChoice choiceContext = new ChoiceContext.PermanentTypeChoice(playerId, destination, entryDescription);

        List<String> permanentTypes = List.of("ARTIFACT", "CREATURE", "ENCHANTMENT", "LAND", "PLANESWALKER");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, permanentTypes, "Choose a permanent type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type", gameData.id, playerName);
    }

    public void beginBasicLandTypeChoice(GameData gameData, UUID playerId, UUID permanentId) {
        beginBasicLandTypeChoice(gameData, playerId, permanentId, false, false);
    }

    /**
     * @param isSecondChoice   true when this is the second of two "as enters" land-type picks
     * @param chainSecondAfter true when answering the first pick should immediately begin the second
     */
    public void beginBasicLandTypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                         boolean isSecondChoice, boolean chainSecondAfter) {
        beginBasicLandTypeChoice(gameData, playerId, permanentId, isSecondChoice, chainSecondAfter, List.of());
    }

    /**
     * @param allowedTypes when non-empty, only these basic land types are offered
     *                     ("choose Island or Swamp" — Roots of Life)
     */
    public void beginBasicLandTypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                         boolean isSecondChoice, boolean chainSecondAfter,
                                         List<CardSubtype> allowedTypes) {
        ChoiceContext.BasicLandTypeChoice choiceContext =
                new ChoiceContext.BasicLandTypeChoice(permanentId, isSecondChoice, chainSecondAfter, allowedTypes);

        List<String> basicLandTypes = allowedTypes.isEmpty()
                ? List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST")
                : allowedTypes.stream().map(Enum::name).toList();
        String prompt = isSecondChoice
                ? "Choose the second basic land type."
                : "Choose a basic land type.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type (second={})", gameData.id, playerName, isSecondChoice);
    }

    public void beginAddBasicLandTypeChoice(GameData gameData, UUID playerId, UUID targetLandId, EffectDuration duration) {
        beginAddBasicLandTypeChoice(gameData, playerId, targetLandId, duration, false);
    }

    public void beginAddBasicLandTypeChoice(GameData gameData, UUID playerId, UUID targetLandId, EffectDuration duration, boolean replacing) {
        ChoiceContext.AddBasicLandTypeChoice choiceContext = new ChoiceContext.AddBasicLandTypeChoice(targetLandId, duration, replacing);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type to add", gameData.id, playerName);
    }

    /**
     * Prompts for the land type of a snow landwalk grant (Barbarian Guides). Only the five basic
     * land types are offered, matching every other land-type choice in the engine.
     */
    public void beginSnowLandwalkTypeChoice(GameData gameData, UUID playerId, UUID targetId) {
        ChoiceContext.SnowLandwalkGrantChoice choiceContext = new ChoiceContext.SnowLandwalkGrantChoice(targetId);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, "Choose a land type for snow landwalk."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a land type for snow landwalk", gameData.id, playerName);
    }

    public void beginOwnLandsBecomeBasicTypeChoice(GameData gameData, UUID playerId) {
        ChoiceContext.OwnLandsBecomeBasicTypeChoice choiceContext = new ChoiceContext.OwnLandsBecomeBasicTypeChoice(playerId);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type for their lands", gameData.id, playerName);
    }

    /**
     * Vision Charm: prompt for a land type first ({@code fromType == null}); after that pick,
     * call again with the chosen {@code fromType} to prompt for the destination basic land type.
     */
    public void beginLandsOfTypeBecomeBasicTypeChoice(GameData gameData, UUID playerId) {
        beginLandsOfTypeBecomeBasicTypeChoice(gameData, playerId, null);
    }

    public void beginLandsOfTypeBecomeBasicTypeChoice(GameData gameData, UUID playerId, CardSubtype fromType) {
        ChoiceContext.LandsOfTypeBecomeBasicTypeChoice choiceContext =
                new ChoiceContext.LandsOfTypeBecomeBasicTypeChoice(playerId, fromType);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        boolean choosingFrom = fromType == null;
        String prompt = choosingFrom
                ? "Choose a land type."
                : "Choose a basic land type.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose {} for lands-of-type become (from={})",
                gameData.id, playerName, choosingFrom ? "a land type" : "a basic land type", fromType);
    }

    public void beginStorageMatrixUntapChoice(GameData gameData, UUID playerId) {
        ChoiceContext.StorageMatrixUntapChoice choiceContext = new ChoiceContext.StorageMatrixUntapChoice(playerId);

        List<String> options = List.of("ARTIFACT", "CREATURE", "LAND");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options, "Choose artifact, creature, or land to untap."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type to untap (Storage Matrix)", gameData.id, playerName);
    }

    /**
     * Teferi's Realm: {@code playerId} chooses artifact, creature, land, or non-Aura enchantment;
     * completing the choice phases out all nontoken permanents of that type.
     */
    public void beginTeferisRealmTypeChoice(GameData gameData, UUID playerId, Card sourceCard) {
        ChoiceContext.TeferisRealmTypeChoice choiceContext =
                new ChoiceContext.TeferisRealmTypeChoice(playerId, sourceCard);

        List<String> options = List.of("ARTIFACT", "CREATURE", "LAND", "NON_AURA_ENCHANTMENT");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose artifact, creature, land, or non-Aura enchantment."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type (Teferi's Realm)", gameData.id, playerName);
    }

    public void beginStaticOrbUntapChoice(GameData gameData, UUID playerId, List<UUID> candidateIds,
                                          int maxUntap,
                                          com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        String prompt = "Choose up to " + maxUntap + " permanent" + (maxUntap == 1 ? "" : "s") + " to untap.";
        beginMultiPermanentChoice(gameData, playerId, candidateIds, maxUntap,
                new MultiPermanentChoiceContext.StaticOrbUntap(playerId, filter),
                prompt);

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} permanent(s) to untap (untap lock)",
                gameData.id, playerName, maxUntap);
    }

    private static List<Integer> allHandIndices(List<Card> hand) {
        return IntStream.range(0, hand.size()).boxed().toList();
    }

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST, CardSubtype.MOUNTAIN, CardSubtype.ISLAND,
            CardSubtype.PLAINS, CardSubtype.SWAMP, CardSubtype.AURA,
            CardSubtype.EQUIPMENT, CardSubtype.LOCUS
    );

    public void beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes) {
        ChoiceContext.CardNameChoice choiceContext = new ChoiceContext.CardNameChoice(card, playerId, excludedTypes);

        List<String> cardNames;
        String prompt;
        if (excludedTypes.isEmpty()) {
            cardNames = collectAllCardNamesInGame(gameData);
            prompt = "Choose a card name.";
        } else {
            cardNames = collectCardNamesInGameExcluding(gameData, excludedTypes);
            String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
            prompt = "Choose a non" + excludedLabel + " card name.";
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card name", gameData.id, playerName);
    }

    /**
     * Asks {@code ctx.choosingPlayerId()} for a card name other than a basic land card name
     * (Null Chamber). Called twice per resolution: once for the controller, then once for their
     * opponent with the first name carried in the context.
     */
    public void beginDualCardNameChoice(GameData gameData, ChoiceContext.DualCardNameChoice ctx) {
        List<String> cardNames = collectNonBasicLandCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                ctx.choosingPlayerId(), null, null, ctx, cardNames,
                "Choose a card name other than a basic land card name."));

        String playerName = gameData.playerIdToName.get(ctx.choosingPlayerId());
        log.info("Game {} - Awaiting {} to choose a card name for {}", gameData.id, playerName, ctx.card().getName());
    }

    public void beginSpellCardNameChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId,
                                         List<CardType> excludedTypes, CardType requiredType) {
        ChoiceContext.ExileByNameChoice choiceContext = new ChoiceContext.ExileByNameChoice(targetPlayerId, choosingPlayerId, excludedTypes);

        List<String> cardNames = collectCardNamesInGameExcluding(gameData, excludedTypes, requiredType);
        String prompt;
        if (requiredType != null) {
            String typeLabel = requiredType.name().toLowerCase();
            String article = "aeiou".indexOf(typeLabel.charAt(0)) >= 0 ? "an " : "a ";
            prompt = "Choose " + article + typeLabel + " card name.";
        } else {
            String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
            prompt = "Choose a non" + excludedLabel + " card name.";
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (exile from zones)", gameData.id, playerName);
    }

    public void beginRevealHandDamageAndExileCardNameChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId,
                                                            List<CardType> excludedTypes, int damagePerCard, Card sourceCard) {
        ChoiceContext.RevealHandDamageAndExileByNameChoice choiceContext =
                new ChoiceContext.RevealHandDamageAndExileByNameChoice(targetPlayerId, choosingPlayerId, excludedTypes, damagePerCard, sourceCard);

        List<String> cardNames = collectCardNamesInGameExcluding(gameData, excludedTypes);
        String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
        String prompt = "Choose a non" + excludedLabel + " card name.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (reveal hand, damage, exile)", gameData.id, playerName);
    }

    /**
     * Shimian Specter: {@code targetPlayerId} reveals their hand and the controller chooses a card
     * in it that isn't of an excluded type. The pick reuses the Thought Hemorrhage answer flow with
     * {@code damagePerCard = 0}, so every copy of the chosen name is exiled from the target's hand,
     * graveyard, and library and they shuffle. Unlike that card the options come from the revealed
     * hand only, so no interaction begins when the hand holds no legal card.
     */
    public void beginRevealHandChooseCardFromItAndExileAllCopiesChoice(GameData gameData, UUID choosingPlayerId,
                                                                       UUID targetPlayerId, List<CardType> excludedTypes,
                                                                       Card sourceCard) {
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        List<String> cardNames = hand == null ? List.of() : hand.stream()
                .filter(card -> !hasExcludedType(card, excludedTypes))
                .map(Card::getName)
                .distinct()
                .sorted()
                .toList();

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (cardNames.isEmpty()) {
            log.info("Game {} - {}'s revealed hand holds no choosable card; nothing is exiled", gameData.id, targetName);
            return;
        }

        ChoiceContext.RevealHandDamageAndExileByNameChoice choiceContext =
                new ChoiceContext.RevealHandDamageAndExileByNameChoice(targetPlayerId, choosingPlayerId, excludedTypes, 0, sourceCard);

        String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
        String prompt = "Choose a non" + excludedLabel + " card from " + targetName + "'s revealed hand.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card from {}'s revealed hand (exile all copies)",
                gameData.id, playerName, targetName);
    }

    public void beginSphinxAmbassadorCardNameChoice(GameData gameData, UUID namingPlayerId, UUID controllerId) {
        ChoiceContext.SphinxAmbassadorNameChoice choiceContext = new ChoiceContext.SphinxAmbassadorNameChoice(namingPlayerId, controllerId);

        List<String> cardNames = collectAllCardNamesInGame(gameData);
        String prompt = "Choose a card name.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                namingPlayerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(namingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (Sphinx Ambassador)", gameData.id, playerName);
    }

    private List<String> collectCardNamesInGameExcluding(GameData gameData, List<CardType> excludedTypes) {
        return collectCardNamesInGameExcluding(gameData, excludedTypes, null);
    }

    private List<String> collectCardNamesInGameExcluding(GameData gameData, List<CardType> excludedTypes, CardType requiredType) {
        return collectCardNamesInGame(gameData, card -> isNameCandidate(card, excludedTypes, requiredType));
    }

    /** Names of every card in the game that isn't a basic land card (Null Chamber). */
    private List<String> collectNonBasicLandCardNamesInGame(GameData gameData) {
        return collectCardNamesInGame(gameData,
                card -> !(card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC)));
    }

    /** Every distinct card name across all zones (battlefield, hand, graveyard, library, exile, stack)
     *  whose card satisfies {@code candidate}, sorted alphabetically. */
    private List<String> collectCardNamesInGame(GameData gameData, Predicate<Card> candidate) {
        Set<String> names = new TreeSet<>();
        for (UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of()).stream()
                    .filter(p -> candidate.test(p.getCard()))
                    .forEach(p -> names.add(p.getCard().getName()));
            gameData.playerHands.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.add(c.getName()));
            gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.add(c.getName()));
            gameData.playerDecks.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.add(c.getName()));
            gameData.getPlayerExiledCards(pid).stream()
                    .filter(candidate)
                    .forEach(c -> names.add(c.getName()));
        }
        gameData.stack.stream()
                .filter(se -> candidate.test(se.getCard()))
                .forEach(se -> names.add(se.getCard().getName()));
        return new ArrayList<>(names);
    }

    /** A card's name is offered only when it has none of {@code excludedTypes} and, if set, has {@code requiredType}. */
    private boolean isNameCandidate(Card card, List<CardType> excludedTypes, CardType requiredType) {
        return !hasExcludedType(card, excludedTypes) && (requiredType == null || card.hasType(requiredType));
    }

    private boolean hasExcludedType(Card card, List<CardType> excludedTypes) {
        if (excludedTypes.contains(card.getType())) {
            return true;
        }
        for (CardType excluded : excludedTypes) {
            if (card.getAdditionalTypes().contains(excluded)) {
                return true;
            }
        }
        return false;
    }

    private List<String> collectAllCardNamesInGame(GameData gameData) {
        return collectCardNamesInGameExcluding(gameData, List.of());
    }

    public void beginMultiZoneExileChoice(GameData gameData, UUID choosingPlayerId, List<Card> matchingCards, UUID targetPlayerId, String cardName) {
        List<UUID> validCardIds = matchingCards.stream().map(Card::getId).toList();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiZoneExileChoice(
                choosingPlayerId, validCardIds, matchingCards.size(), targetPlayerId, choosingPlayerId, cardName));
    }

    /**
     * Begin a mixed battlefield + hand exile selection (Descent into Madness). The interaction
     * carries its own APNAP queue state, so the effect handler builds it and passes it whole.
     */
    public void beginExilePermanentsOrHandCardsChoice(GameData gameData,
            PendingInteraction.ExilePermanentsOrHandCardsChoice interaction) {
        interactionHandlerRegistry.begin(gameData, interaction);
    }

    /**
     * Begin the mixed battlefield + graveyard + hand Aura selection of Bruna, Light of Alabaster.
     * The candidate list is built by the effect handler, so the interaction arrives whole.
     */
    public void beginAttachAurasChoice(GameData gameData, PendingInteraction.AttachAurasChoice interaction) {
        interactionHandlerRegistry.begin(gameData, interaction);
    }

    public void beginImprintFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID sourcePermanentId) {
        beginImprintFromHandChoice(gameData, playerId, validIndices, prompt, sourcePermanentId, false);
    }

    public void beginImprintFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                           UUID sourcePermanentId, boolean grantCastPermission) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ImprintFromHandChoice(
                playerId, new ArrayList<>(validIndices), sourcePermanentId, prompt, grantCastPermission));
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId, int remainingCount) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, null, remainingCount);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, List.of(), 0);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, remainingChoosers, cardsPerPlayer, false);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer,
                                         boolean faceDown) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = allHandIndices(hand);

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExileFromHandChoice(
                playerId, validIndices, sourcePermanentId, playPermissionControllerId, remainingCount,
                "Choose a card to exile.",
                remainingChoosers != null ? remainingChoosers : List.of(),
                cardsPerPlayer, faceDown));
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount) {
        beginDiscardChoice(gameData, playerId, remainingCount, DiscardFollowUp.NONE);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount, DiscardFollowUp followUp) {
        List<Card> hand = gameData.playerHands.get(playerId);
        beginDiscardChoice(gameData, playerId, allHandIndices(hand), "Choose a card to discard.", remainingCount, followUp);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, int remainingCount) {
        beginDiscardChoice(gameData, playerId, validIndices, prompt, remainingCount, DiscardFollowUp.NONE);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, int remainingCount, DiscardFollowUp followUp) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardChoice(
                playerId, new ArrayList<>(validIndices), remainingCount, followUp, prompt));
    }

    public void processNextMayAbility(GameData gameData) {
        if (gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (gameData.status == GameStatus.FINISHED) {
            gameData.pendingMayAbilities.clear();
            return;
        }

        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MayAbilityChoice(
                next.controllerId(), next.description(), next.manaCost()));
    }
}


