package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingSphinxAmbassadorChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorPutOnBattlefieldEffect;
import java.util.Collections;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChoiceHandlerService {

    private final SessionManager sessionManager;
    private final GameQueryService gameQueryService;
    private final WarpWorldService warpWorldService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final LegendRuleService legendRuleService;

    public void handleListChoice(GameData gameData, Player player, String colorName) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.COLOR_CHOICE)) {
            throw new IllegalStateException("Not awaiting color choice");
        }
        InteractionContext.ColorChoice colorChoice = gameData.interaction.colorChoiceContextView();
        if (colorChoice == null || !player.getId().equals(colorChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Mana color choice (Chromatic Star, etc.)
        if (colorChoice.context() instanceof ChoiceContext.ManaColorChoice ctx) {
            handleManaColorChosen(gameData, player, colorName, ctx);
            return;
        }

        // Attack mana split choice (Grand Warlord Radha, etc.)
        if (colorChoice.context() instanceof ChoiceContext.AttackManaSplitChoice ctx) {
            handleAttackManaSplitChosen(gameData, player, colorName, ctx);
            return;
        }

        // Card name choice (Pithing Needle, etc.)
        if (colorChoice.context() instanceof ChoiceContext.CardNameChoice ctx) {
            handleCardNameChosen(gameData, player, colorName, ctx);
            return;
        }

        // Text-changing effects (Mind Bend, etc.) — two-step color/land-type choice
        if (colorChoice.context() instanceof ChoiceContext.TextChangeFromWord ctx) {
            handleTextChangeFromWordChosen(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.TextChangeToWord ctx) {
            handleTextChangeToWordChosen(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.DrawReplacementChoice ctx) {
            handleDrawReplacementChoice(gameData, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.KeywordGrantChoice ctx) {
            handleKeywordGrantChoice(gameData, player, colorName, ctx);
            return;
        }

        if (colorChoice.context() instanceof ChoiceContext.ExileByNameChoice ctx) {
            handleExileByNameChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ProtectionColorChoice ctx) {
            handleProtectionColorChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.SubtypeChoice ctx) {
            handleSubtypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.BasicLandTypeChoice ctx) {
            handleBasicLandTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.AddBasicLandTypeChoice ctx) {
            handleAddBasicLandTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.PermanentTypeChoice ctx) {
            handlePermanentTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.EachPlayerCardNameRevealChoice ctx) {
            handleEachPlayerCardNameRevealChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.SphinxAmbassadorNameChoice ctx) {
            handleSphinxAmbassadorNameChoice(gameData, player, colorName, ctx);
            return;
        }
        CardColor color = CardColor.valueOf(colorName);
        UUID permanentId = colorChoice.permanentId();
        UUID etbTargetId = colorChoice.etbTargetId();

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

    private void handleManaColorChosen(GameData gameData, Player player, String colorName, ChoiceContext.ManaColorChoice ctx) {
        ManaColor manaColor = ManaColor.valueOf(colorName);

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        ManaPool manaPool = gameData.playerManaPools.get(ctx.playerId());
        int amount = ctx.amount();
        manaPool.add(manaColor, amount);
        if (ctx.fromCreature()) {
            manaPool.addCreatureMana(manaColor, amount);
        }

        String manaWord = amount == 1 ? "one" : String.valueOf(amount);
        String logEntry = player.getUsername() + " adds " + manaWord + " " + colorName.toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} adds {} {} mana", gameData.id, player.getUsername(), manaWord, colorName.toLowerCase());

        gameData.priorityPassedBy.clear();
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleAttackManaSplitChosen(GameData gameData, Player player, String colorName, ChoiceContext.AttackManaSplitChoice ctx) {
        ManaColor manaColor = ManaColor.valueOf(colorName);

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        ManaPool manaPool = gameData.playerManaPools.get(ctx.playerId());
        // Add as persistent mana — doesn't drain at step/phase transitions until end of turn
        manaPool.addPersistentMana(manaColor, ctx.attackerCount());

        String logEntry = player.getUsername() + " adds " + ctx.attackerCount() + " " + colorName.toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} adds {} {} mana (attacking creatures, persistent until end of turn)",
                gameData.id, player.getUsername(), ctx.attackerCount(), colorName.toLowerCase());

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleTextChangeFromWordChosen(GameData gameData, Player player, String chosenWord, ChoiceContext.TextChangeFromWord ctx) {
        boolean isColor = GameQueryService.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord);
        boolean isLandType = GameQueryService.TEXT_CHANGE_LAND_TYPES.contains(chosenWord);
        if (!isColor && !isLandType) {
            throw new IllegalArgumentException("Invalid choice: " + chosenWord);
        }

        ChoiceContext.TextChangeToWord choiceContext =
                new ChoiceContext.TextChangeToWord(ctx.targetId(), chosenWord, isColor);
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

        sessionManager.sendToPlayer(player.getId(), new ChooseFromListMessage(remainingOptions, "Choose the replacement " + promptType + "."));
        log.info("Game {} - Awaiting {} to choose replacement word for text change", gameData.id, player.getUsername());
    }

    private void handleTextChangeToWordChosen(GameData gameData, Player player, String chosenWord, ChoiceContext.TextChangeToWord ctx) {
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

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
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

    private void handleCardNameChosen(GameData gameData, Player player, String cardName, ChoiceContext.CardNameChoice ctx) {
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

    private void handleKeywordGrantChoice(GameData gameData, Player player, String chosenKeywordName, ChoiceContext.KeywordGrantChoice ctx) {
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

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
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

    private void handleDrawReplacementChoice(GameData gameData, String chosenKind, ChoiceContext.DrawReplacementChoice ctx) {
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
            boolean isLand = top.hasType(CardType.LAND);
            if ((chooseLand && isLand) || (!chooseLand && !isLand)) {
                chosenCard = top;
                break;
            }
        }

        String revealedNames = String.join(", ", revealed.stream().map(Card::getName).toList());
        gameBroadcastService.logAndBroadcast(gameData, playerName + " reveals " + revealedNames + " for Abundance.");

        List<Card> toBottom = new ArrayList<>(revealed);
        if (chosenCard != null) {
            gameData.addCardToHand(playerId, chosenCard);
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

    private void handleProtectionColorChoice(GameData gameData, Player player, String chosenValue, ChoiceContext.ProtectionColorChoice ctx) {
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
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

    private void handleSubtypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.SubtypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenSubtype(subtype);

            String logEntry = player.getUsername() + " chooses " + subtype.getDisplayName() + " for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} chooses creature type {} for {}", gameData.id, player.getUsername(), subtype, perm.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleBasicLandTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.BasicLandTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenSubtype(subtype);

            String logEntry = player.getUsername() + " chooses " + subtype.getDisplayName() + " for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} chooses basic land type {} for {}", gameData.id, player.getUsername(), subtype, perm.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleAddBasicLandTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.AddBasicLandTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        Permanent targetLand = gameQueryService.findPermanentById(gameData, ctx.targetLandId());
        if (targetLand != null) {
            ManaColor manaColor = EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(subtype);
            ActivatedAbility manaAbility = new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(manaColor)),
                    "{T}: Add {" + manaColor.getCode() + "}.");

            if (ctx.duration() == EffectDuration.UNTIL_END_OF_TURN) {
                // Transient: cleared at end of turn by resetModifiers()
                if (!targetLand.getTransientSubtypes().contains(subtype)) {
                    targetLand.getTransientSubtypes().add(subtype);
                }
                targetLand.getTemporaryActivatedAbilities().add(manaAbility);
            } else {
                // Permanent: survives turn resets
                if (!targetLand.getGrantedSubtypes().contains(subtype)) {
                    targetLand.getGrantedSubtypes().add(subtype);
                }
                targetLand.getCard().addActivatedAbility(manaAbility);
            }

            String durationText = ctx.duration() == EffectDuration.UNTIL_END_OF_TURN ? " until end of turn" : "";
            String logEntry = targetLand.getCard().getName() + " becomes a " + subtype.getDisplayName()
                    + " in addition to its other types" + durationText + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} becomes a {}{}", gameData.id, targetLand.getCard().getName(), subtype, durationText);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handlePermanentTypeChoice(GameData gameData, Player player, String typeName, ChoiceContext.PermanentTypeChoice ctx) {
        CardType chosenType = CardType.valueOf(typeName);
        if (!chosenType.isPermanentType()) {
            throw new IllegalArgumentException("Invalid permanent type choice: " + typeName);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        UUID controllerId = ctx.controllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);

        if (graveyard == null || graveyard.isEmpty()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        List<Card> toReturn = new ArrayList<>();
        for (Card card : graveyard) {
            if (card.hasType(chosenType)) {
                toReturn.add(card);
            }
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        if (!toReturn.isEmpty()) {
            List<String> returnedNames = new ArrayList<>();
            for (Card card : toReturn) {
                graveyard.remove(card);
                gameData.addCardToHand(controllerId, card);
                returnedNames.add(card.getName());
            }

            String logEntry = playerName + " chooses " + chosenType.getDisplayName()
                    + " and returns " + String.join(", ", returnedNames) + " to hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} chooses {} and returns {} card(s) from graveyard to hand",
                    gameData.id, playerName, chosenType.getDisplayName(), returnedNames.size());
        } else {
            String logEntry = playerName + " chooses " + chosenType.getDisplayName()
                    + " but has no " + chosenType.getDisplayName().toLowerCase() + " cards in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleEachPlayerCardNameRevealChoice(GameData gameData, Player player, String cardName,
                                                      ChoiceContext.EachPlayerCardNameRevealChoice ctx) {
        // Store this player's chosen name
        Map<UUID, String> updatedNames = new LinkedHashMap<>(ctx.chosenNames());
        updatedNames.put(player.getId(), cardName);

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\".";
        gameBroadcastService.logAndBroadcast(gameData, choiceLog);
        log.info("Game {} - {} chooses card name \"{}\" (each player name/reveal)",
                gameData.id, player.getUsername(), cardName);

        // Check if more players need to name a card
        UUID nextPlayerId = null;
        for (UUID pid : ctx.playerOrder()) {
            if (!updatedNames.containsKey(pid)) {
                nextPlayerId = pid;
                break;
            }
        }

        if (nextPlayerId != null) {
            // Prompt next player
            gameData.interaction.clearAwaitingInput();
            gameData.interaction.clearColorChoice();

            var nextContext = new ChoiceContext.EachPlayerCardNameRevealChoice(
                    ctx.playerOrder(), updatedNames);
            gameData.interaction.beginColorChoice(nextPlayerId, null, null, nextContext);

            List<String> cardNames = collectAllCardNamesInGame(gameData);
            sessionManager.sendToPlayer(nextPlayerId, new ChooseFromListMessage(cardNames, "Choose a card name."));

            String nextPlayerName = gameData.playerIdToName.get(nextPlayerId);
            log.info("Game {} - Awaiting {} to choose a card name (each player name/reveal)",
                    gameData.id, nextPlayerName);
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }

        // All players have named — resolve reveals
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        for (UUID pid : ctx.playerOrder()) {
            String chosenName = updatedNames.get(pid);
            String playerName = gameData.playerIdToName.get(pid);
            List<Card> deck = gameData.playerDecks.get(pid);

            if (deck == null || deck.isEmpty()) {
                String logEntry = playerName + "'s library is empty.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                continue;
            }

            Card topCard = deck.removeFirst();
            String revealLog = playerName + " reveals " + topCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, revealLog);

            if (topCard.getName().equals(chosenName)) {
                gameData.addCardToHand(pid, topCard);
                String handLog = playerName + " puts " + topCard.getName() + " into their hand.";
                gameBroadcastService.logAndBroadcast(gameData, handLog);
                log.info("Game {} - {} guessed correctly, {} goes to hand", gameData.id, playerName, topCard.getName());
            } else {
                deck.add(topCard);
                String bottomLog = playerName + " puts " + topCard.getName() + " on the bottom of their library.";
                gameBroadcastService.logAndBroadcast(gameData, bottomLog);
                log.info("Game {} - {} guessed wrong, {} goes to bottom", gameData.id, playerName, topCard.getName());
            }
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private List<String> collectAllCardNamesInGame(GameData gameData) {
        Set<String> names = new TreeSet<>();
        for (UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of())
                    .forEach(p -> names.add(p.getCard().getName()));
            gameData.playerHands.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerGraveyards.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerDecks.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerExiledCards.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
        }
        gameData.stack.forEach(se -> names.add(se.getCard().getName()));
        return new ArrayList<>(names);
    }

    private void handleExileByNameChoice(GameData gameData, Player player, String cardName, ChoiceContext.ExileByNameChoice ctx) {
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

    private void handleSphinxAmbassadorNameChoice(GameData gameData, Player player, String cardName, ChoiceContext.SphinxAmbassadorNameChoice ctx) {
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearColorChoice();

        PendingSphinxAmbassadorChoice pending = gameData.pendingSphinxAmbassadorChoice;
        if (pending == null || pending.selectedCard() == null) {
            throw new IllegalStateException("No pending Sphinx Ambassador choice");
        }

        Card selectedCard = pending.selectedCard();
        UUID controllerId = pending.controllerId();
        UUID targetPlayerId = pending.targetPlayerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        String choiceLog = targetName + " chooses \"" + cardName + "\".";
        gameBroadcastService.logAndBroadcast(gameData, choiceLog);
        log.info("Game {} - {} chooses card name \"{}\" for Sphinx Ambassador", gameData.id, targetName, cardName);

        boolean isCreature = selectedCard.hasType(CardType.CREATURE);
        boolean nameDoesNotMatch = !selectedCard.getName().equals(cardName);

        if (isCreature && nameDoesNotMatch) {
            // Conditions met — present may ability to controller: "You may put it onto the battlefield"
            String prompt = pending.sourceCard().getName() + " — Put " + selectedCard.getName()
                    + " onto the battlefield under your control?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    pending.sourceCard(), controllerId,
                    List.of(new SphinxAmbassadorPutOnBattlefieldEffect()),
                    prompt
            ));
            playerInputService.processNextMayAbility(gameData);
        } else {
            // Conditions not met — card stays in library without being revealed (per ruling)
            String logMsg = "The conditions for " + pending.sourceCard().getName() + " are not met. "
                    + targetName + "'s library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - Sphinx Ambassador: selected card does not match conditions (creature={}, nameMatch={})",
                    gameData.id, isCreature, !nameDoesNotMatch);

            gameData.playerDecks.get(targetPlayerId).add(selectedCard);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameData.pendingSphinxAmbassadorChoice = null;

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
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


