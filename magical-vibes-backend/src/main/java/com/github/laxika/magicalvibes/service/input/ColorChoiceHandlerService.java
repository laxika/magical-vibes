package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColorChoiceHandlerService {

    private final SessionManager sessionManager;
    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;

    public void handleColorChosen(GameData gameData, Player player, String colorName) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.COLOR_CHOICE)) {
            throw new IllegalStateException("Not awaiting color choice");
        }
        InteractionContext.ColorChoice colorChoice = gameData.interaction.colorChoiceContextView();
        if (colorChoice == null || !player.getId().equals(colorChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Mana color choice (Chromatic Star, etc.)
        if (colorChoice.context() instanceof ColorChoiceContext.ManaColorChoice ctx) {
            handleManaColorChosen(gameData, player, colorName, ctx);
            return;
        }

        // Text-changing effects (Mind Bend, etc.) — two-step color/land-type choice
        if (colorChoice.context() instanceof ColorChoiceContext.TextChangeFromWord ctx) {
            handleTextChangeFromWordChosen(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ColorChoiceContext.TextChangeToWord ctx) {
            handleTextChangeToWordChosen(gameData, player, colorName, ctx);
            return;
        }

        CardColor color = CardColor.valueOf(colorName);
        UUID permanentId = colorChoice.permanentId();
        UUID etbTargetId = colorChoice.etbTargetPermanentId();

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Permanent perm = gameQueryService.findPermanentById(gameData, permanentId);
        if (perm != null) {
            perm.setChosenColor(color);

            String logEntry = player.getUsername() + " chooses " + color.name().toLowerCase() + " for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), color, perm.getCard().getName());

            if (gameQueryService.isCreature(gameData, perm)) {
                gameHelper.processCreatureETBEffects(gameData, player.getId(), perm.getCard(), etbTargetId);
            }
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleManaColorChosen(GameData gameData, Player player, String colorName, ColorChoiceContext.ManaColorChoice ctx) {
        ManaColor manaColor = ManaColor.valueOf(colorName);

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        ManaPool manaPool = gameData.playerManaPools.get(ctx.playerId());
        manaPool.add(manaColor);

        String logEntry = player.getUsername() + " adds one " + colorName.toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} adds one {} mana", gameData.id, player.getUsername(), colorName.toLowerCase());

        gameData.priorityPassedBy.clear();
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleTextChangeFromWordChosen(GameData gameData, Player player, String chosenWord, ColorChoiceContext.TextChangeFromWord ctx) {
        boolean isColor = GameQueryService.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord);
        boolean isLandType = GameQueryService.TEXT_CHANGE_LAND_TYPES.contains(chosenWord);
        if (!isColor && !isLandType) {
            throw new IllegalArgumentException("Invalid choice: " + chosenWord);
        }

        ColorChoiceContext.TextChangeToWord choiceContext =
                new ColorChoiceContext.TextChangeToWord(ctx.targetPermanentId(), chosenWord, isColor);
        gameData.interaction.beginColorChoice(player.getId(), null, null, choiceContext);

        List<String> remainingOptions;
        String promptType;
        if (isColor) {
            remainingOptions = GameQueryService.TEXT_CHANGE_COLOR_WORDS.stream().filter(c -> !c.equals(chosenWord)).toList();
            promptType = "color word";
        } else {
            remainingOptions = GameQueryService.TEXT_CHANGE_LAND_TYPES.stream().filter(t -> !t.equals(chosenWord)).toList();
            promptType = "basic land type";
        }

        sessionManager.sendToPlayer(player.getId(), new ChooseColorMessage(remainingOptions, "Choose the replacement " + promptType + "."));
        log.info("Game {} - Awaiting {} to choose replacement word for text change", gameData.id, player.getUsername());
    }

    private void handleTextChangeToWordChosen(GameData gameData, Player player, String chosenWord, ColorChoiceContext.TextChangeToWord ctx) {
        if (ctx.isColor()) {
            if (!GameQueryService.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid color choice: " + chosenWord);
            }
        } else {
            if (!GameQueryService.TEXT_CHANGE_LAND_TYPES.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid land type choice: " + chosenWord);
            }
        }

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        if (target != null) {
            String fromText = textChangeChoiceToWord(ctx.fromWord());
            String toText = textChangeChoiceToWord(chosenWord);
            target.getTextReplacements().add(new TextReplacement(fromText, toText));

            // If the permanent has a chosenColor matching the from-color, update it
            if (ctx.isColor()) {
                CardColor fromColor = CardColor.valueOf(ctx.fromWord());
                CardColor toColor = CardColor.valueOf(chosenWord);
                if (fromColor.equals(target.getChosenColor())) {
                    target.setChosenColor(toColor);
                }
            }

            String logEntry = player.getUsername() + " changes all instances of " + fromText + " to " + toText + " on " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} changes {} to {} on {}", gameData.id, player.getUsername(), fromText, toText, target.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private String textChangeChoiceToWord(String choice) {
        return switch (choice) {
            case "WHITE" -> "white";
            case "BLUE" -> "blue";
            case "BLACK" -> "black";
            case "RED" -> "red";
            case "GREEN" -> "green";
            case "PLAINS" -> "Plains";
            case "ISLAND" -> "Island";
            case "SWAMP" -> "Swamp";
            case "MOUNTAIN" -> "Mountain";
            case "FOREST" -> "Forest";
            default -> throw new IllegalArgumentException("Invalid choice: " + choice);
        };
    }
}

