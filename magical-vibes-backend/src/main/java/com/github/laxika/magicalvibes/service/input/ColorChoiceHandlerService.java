package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import java.util.Collections;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColorChoiceHandlerService {

    private final SessionManager sessionManager;
    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final WarpWorldService warpWorldService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final LegendRuleService legendRuleService;

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

        // Card name choice (Pithing Needle, etc.)
        if (colorChoice.context() instanceof ColorChoiceContext.CardNameChoice ctx) {
            handleCardNameChosen(gameData, player, colorName, ctx);
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
        if (colorChoice.context() instanceof ColorChoiceContext.DrawReplacementChoice ctx) {
            handleDrawReplacementChoice(gameData, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ColorChoiceContext.KeywordGrantChoice ctx) {
            handleKeywordGrantChoice(gameData, player, colorName, ctx);
            return;
        }

        if (colorChoice.context() instanceof ColorChoiceContext.ExileByNameChoice ctx) {
            handleExileByNameChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ColorChoiceContext.ProtectionColorChoice ctx) {
            handleProtectionColorChoice(gameData, player, colorName, ctx);
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
                battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), perm.getCard(), etbTargetId, false);
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

    private void handleCardNameChosen(GameData gameData, Player player, String cardName, ColorChoiceContext.CardNameChoice ctx) {
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Card card = ctx.card();
        UUID controllerId = ctx.controllerId();

        Permanent perm = new Permanent(card);
        perm.setChosenName(cardName);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\" for " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, choiceLog);
        log.info("Game {} - {} chooses card name \"{}\" for {}", gameData.id, player.getUsername(), cardName, card.getName());

        legendRuleService.checkLegendRule(gameData, controllerId);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleKeywordGrantChoice(GameData gameData, Player player, String chosenKeywordName, ColorChoiceContext.KeywordGrantChoice ctx) {
        Keyword keyword;
        try {
            keyword = Keyword.valueOf(chosenKeywordName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid keyword choice: " + chosenKeywordName);
        }
        if (!ctx.options().contains(keyword)) {
            throw new IllegalArgumentException("Keyword not among valid options: " + chosenKeywordName);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        if (target != null) {
            target.getGrantedKeywords().add(keyword);

            String keywordName = keyword.name().charAt(0) + keyword.name().substring(1).toLowerCase().replace('_', ' ');
            String logEntry = target.getCard().getName() + " gains " + keywordName + " until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), keywordName, target.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleDrawReplacementChoice(GameData gameData, String chosenKind, ColorChoiceContext.DrawReplacementChoice ctx) {
        if (ctx.kind() != DrawReplacementKind.ABUNDANCE) {
            throw new IllegalStateException("Unsupported draw replacement choice kind: " + ctx.kind());
        }
        boolean chooseLand;
        if ("LAND".equals(chosenKind)) {
            chooseLand = true;
        } else if ("NONLAND".equals(chosenKind)) {
            chooseLand = false;
        } else {
            throw new IllegalArgumentException("Invalid Abundance choice: " + chosenKind);
        }

        UUID playerId = ctx.playerId();
        List<Card> deck = gameData.playerDecks.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, playerName + " has no cards to reveal for Abundance.");
            finalizeAfterDrawReplacementChoice(gameData);
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card chosenCard = null;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            revealed.add(top);
            boolean isLand = top.getType() == CardType.LAND || top.getAdditionalTypes().contains(CardType.LAND);
            if ((chooseLand && isLand) || (!chooseLand && !isLand)) {
                chosenCard = top;
                break;
            }
        }

        String revealedNames = String.join(", ", revealed.stream().map(Card::getName).toList());
        gameBroadcastService.logAndBroadcast(gameData, playerName + " reveals " + revealedNames + " for Abundance.");

        List<Card> toBottom = new ArrayList<>(revealed);
        if (chosenCard != null) {
            hand.add(chosenCard);
            toBottom.remove(chosenCard);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " puts " + chosenCard.getName() + " into their hand.");
        } else {
            String missingKind = chooseLand ? "land" : "nonland";
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " reveals no " + missingKind + " card for Abundance.");
        }

        if (toBottom.size() == 1) {
            deck.add(toBottom.getFirst());
        } else if (toBottom.size() > 1) {
            gameData.pendingLibraryBottomReorders.addLast(new LibraryBottomReorderRequest(playerId, toBottom));
            if (!gameData.interaction.isAwaitingInput()) {
                warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
            }
        }

        finalizeAfterDrawReplacementChoice(gameData);
    }

    private void finalizeAfterDrawReplacementChoice(GameData gameData) {
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }
        gameBroadcastService.broadcastGameState(gameData);
        if (!gameData.interaction.isAwaitingInput()) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleProtectionColorChoice(GameData gameData, Player player, String chosenValue, ColorChoiceContext.ProtectionColorChoice ctx) {
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        if (target != null) {
            if ("ARTIFACT".equals(chosenValue)) {
                target.getProtectionFromCardTypes().add(CardType.ARTIFACT);
                String logEntry = target.getCard().getName() + " gains protection from artifacts until end of turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} gains protection from artifacts until end of turn", gameData.id, target.getCard().getName());
            } else {
                CardColor color = CardColor.valueOf(chosenValue);
                target.getProtectionFromColorsUntilEndOfTurn().add(color);
                String colorName = color.name().charAt(0) + color.name().substring(1).toLowerCase();
                String logEntry = target.getCard().getName() + " gains protection from " + colorName.toLowerCase() + " until end of turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} gains protection from {} until end of turn", gameData.id, target.getCard().getName(), colorName.toLowerCase());
            }
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleExileByNameChoice(GameData gameData, Player player, String cardName, ColorChoiceContext.ExileByNameChoice ctx) {
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        UUID targetPlayerId = ctx.targetPlayerId();
        UUID controllerId = ctx.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        String choiceLog = controllerName + " chooses \"" + cardName + "\".";
        gameBroadcastService.logAndBroadcast(gameData, choiceLog);
        log.info("Game {} - {} chooses card name \"{}\" for exile from zones", gameData.id, controllerName, cardName);

        // Collect all matching cards across hand, graveyard, and library
        List<Card> matchingCards = new ArrayList<>();

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand != null) {
            matchingCards.addAll(hand.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            matchingCards.addAll(graveyard.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library != null) {
            matchingCards.addAll(library.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        if (matchingCards.isEmpty()) {
            // No matching cards — just shuffle library and resolve
            if (library != null) {
                Collections.shuffle(library);
            }

            String exileLog = controllerName + " exiles 0 cards named \"" + cardName + "\" from " + targetName
                    + "'s hand, graveyard, and library. " + targetName + " shuffles their library.";
            gameBroadcastService.logAndBroadcast(gameData, exileLog);
            log.info("Game {} - {} found 0 cards named \"{}\" in {}'s zones", gameData.id, controllerName, cardName, targetName);

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        // Present matching cards for "any number" selection
        playerInputService.beginMultiZoneExileChoice(gameData, controllerId, matchingCards, targetPlayerId, cardName);
        gameBroadcastService.broadcastGameState(gameData);
    }

    public void handleMultiZoneExileCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.MULTI_ZONE_EXILE_CHOICE)) {
            throw new IllegalStateException("Not awaiting multi-zone exile choice");
        }
        InteractionContext.MultiZoneExileChoice ctx = gameData.interaction.multiZoneExileChoiceContext();
        if (ctx == null || !player.getId().equals(ctx.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Validate selected card IDs against valid set
        Set<UUID> validIds = ctx.validCardIds();
        for (UUID id : cardIds) {
            if (!validIds.contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearMultiZoneExileChoice();

        UUID targetPlayerId = ctx.targetPlayerId();
        UUID controllerId = ctx.controllerId();
        String cardName = ctx.cardName();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        Set<UUID> selectedIds = new java.util.HashSet<>(cardIds);
        List<Card> exiled = gameData.playerExiledCards.get(targetPlayerId);
        int exiledCount = 0;

        // Remove selected cards from hand
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand != null) {
            List<Card> toExile = hand.stream().filter(c -> selectedIds.contains(c.getId())).toList();
            hand.removeAll(toExile);
            exiled.addAll(toExile);
            exiledCount += toExile.size();
        }

        // Remove selected cards from graveyard
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            List<Card> toExile = graveyard.stream().filter(c -> selectedIds.contains(c.getId())).toList();
            graveyard.removeAll(toExile);
            exiled.addAll(toExile);
            exiledCount += toExile.size();
        }

        // Remove selected cards from library
        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library != null) {
            List<Card> toExile = library.stream().filter(c -> selectedIds.contains(c.getId())).toList();
            library.removeAll(toExile);
            exiled.addAll(toExile);
            exiledCount += toExile.size();
        }

        // Always shuffle target player's library
        if (library != null) {
            Collections.shuffle(library);
        }

        String exileLog = controllerName + " exiles " + exiledCount + " card" + (exiledCount != 1 ? "s" : "")
                + " named \"" + cardName + "\" from " + targetName + "'s hand, graveyard, and library. "
                + targetName + " shuffles their library.";
        gameBroadcastService.logAndBroadcast(gameData, exileLog);
        log.info("Game {} - {} exiled {} card(s) named \"{}\" from {}'s zones",
                gameData.id, controllerName, exiledCount, cardName, targetName);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }
}


