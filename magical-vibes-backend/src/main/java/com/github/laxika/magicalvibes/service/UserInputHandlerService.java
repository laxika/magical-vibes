package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.SpellTypeTargetFilter;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.model.CardView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInputHandlerService {

    private final SessionManager sessionManager;
    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;
    private final TurnProgressionService turnProgressionService;

    void handleColorChosen(GameData gameData, Player player, String colorName) {
        if (gameData.awaitingInput != AwaitingInput.COLOR_CHOICE) {
            throw new IllegalStateException("Not awaiting color choice");
        }
        if (!player.getId().equals(gameData.awaitingColorChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Mana color choice (Chromatic Star, etc.)
        if (gameData.colorChoiceContext instanceof ColorChoiceContext.ManaColorChoice ctx) {
            handleManaColorChosen(gameData, player, colorName, ctx);
            return;
        }

        // Text-changing effects (Mind Bend, etc.) — two-step color/land-type choice
        if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeFromWord ctx) {
            handleTextChangeFromWordChosen(gameData, player, colorName, ctx);
            return;
        }
        if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeToWord ctx) {
            handleTextChangeToWordChosen(gameData, player, colorName, ctx);
            return;
        }

        CardColor color = CardColor.valueOf(colorName);
        UUID permanentId = gameData.awaitingColorChoicePermanentId;
        UUID etbTargetId = gameData.pendingColorChoiceETBTargetId;

        gameData.awaitingInput = null;
        gameData.awaitingColorChoicePlayerId = null;
        gameData.awaitingColorChoicePermanentId = null;
        gameData.pendingColorChoiceETBTargetId = null;

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

    void handleManaColorChosen(GameData gameData, Player player, String colorName, ColorChoiceContext.ManaColorChoice ctx) {
        ManaColor manaColor = ManaColor.valueOf(colorName);

        gameData.colorChoiceContext = null;
        gameData.awaitingInput = null;
        gameData.awaitingColorChoicePlayerId = null;

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

    void handleTextChangeFromWordChosen(GameData gameData, Player player, String chosenWord, ColorChoiceContext.TextChangeFromWord ctx) {
        boolean isColor = GameQueryService.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord);
        boolean isLandType = GameQueryService.TEXT_CHANGE_LAND_TYPES.contains(chosenWord);
        if (!isColor && !isLandType) {
            throw new IllegalArgumentException("Invalid choice: " + chosenWord);
        }

        gameData.colorChoiceContext = new ColorChoiceContext.TextChangeToWord(ctx.targetPermanentId(), chosenWord, isColor);

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

    void handleTextChangeToWordChosen(GameData gameData, Player player, String chosenWord, ColorChoiceContext.TextChangeToWord ctx) {
        if (ctx.isColor()) {
            if (!GameQueryService.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid color choice: " + chosenWord);
            }
        } else {
            if (!GameQueryService.TEXT_CHANGE_LAND_TYPES.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid land type choice: " + chosenWord);
            }
        }

        gameData.awaitingInput = null;
        gameData.awaitingColorChoicePlayerId = null;
        gameData.colorChoiceContext = null;

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

    String textChangeChoiceToWord(String choice) {
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

    void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.awaitingInput == AwaitingInput.DISCARD_CHOICE) {
            handleDiscardCardChosen(gameData, player, cardIndex);
            return;
        }

        if (gameData.awaitingInput == AwaitingInput.REVEALED_HAND_CHOICE) {
            handleRevealedHandCardChosen(gameData, player, cardIndex);
            return;
        }

        if (gameData.awaitingInput != AwaitingInput.CARD_CHOICE && gameData.awaitingInput != AwaitingInput.TARGETED_CARD_CHOICE) {
            throw new IllegalStateException("Not awaiting card choice");
        }
        if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        boolean isTargeted = gameData.awaitingInput == AwaitingInput.TARGETED_CARD_CHOICE;

        gameData.awaitingInput = null;
        gameData.awaitingCardChoicePlayerId = null;
        gameData.awaitingCardChoiceValidIndices = null;

        UUID targetPermanentId = gameData.pendingCardChoiceTargetPermanentId;
        gameData.pendingCardChoiceTargetPermanentId = null;

        if (cardIndex == -1) {
            String logEntry = player.getUsername() + " chooses not to put a card onto the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to put a card onto the battlefield", gameData.id, player.getUsername());
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            Card card = hand.remove(cardIndex);

            if (isTargeted) {
                resolveTargetedCardChoice(gameData, player, playerId, hand, card, targetPermanentId);
            } else {
                resolveUntargetedCardChoice(gameData, player, playerId, hand, card);
            }
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    void handleDiscardCardChosen(GameData gameData, Player player, int cardIndex) {
        if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        gameData.playerGraveyards.get(playerId).add(card);

        String logEntry = player.getUsername() + " discards " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discards {}", gameData.id, player.getUsername(), card.getName());

        gameData.awaitingDiscardRemainingCount--;

        if (gameData.awaitingDiscardRemainingCount > 0 && !hand.isEmpty()) {
            playerInputService.beginDiscardChoice(gameData, playerId);
        } else {
            gameData.awaitingInput = null;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;
            gameData.awaitingDiscardRemainingCount = 0;
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    void handleRevealedHandCardChosen(GameData gameData, Player player, int cardIndex) {
        if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = gameData.awaitingRevealedHandChoiceTargetPlayerId;
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        Card chosenCard = targetHand.remove(cardIndex);
        gameData.awaitingRevealedHandChosenCards.add(chosenCard);

        String logEntry = player.getUsername() + " chooses " + chosenCard.getName() + " from " + targetName + "'s hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} chooses {} from {}'s hand", gameData.id, player.getUsername(), chosenCard.getName(), targetName);

        gameData.awaitingRevealedHandChoiceRemainingCount--;

        boolean discardMode = gameData.awaitingRevealedHandChoiceDiscardMode;

        if (gameData.awaitingRevealedHandChoiceRemainingCount > 0 && !targetHand.isEmpty()) {
            // More cards to choose — update valid indices and prompt again
            List<Integer> newValidIndices = new ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                newValidIndices.add(i);
            }

            String prompt = discardMode
                    ? "Choose another card to discard."
                    : "Choose another card to put on top of " + targetName + "'s library.";
            playerInputService.beginRevealedHandChoice(gameData, player.getId(), targetPlayerId, newValidIndices, prompt);
        } else {
            // All cards chosen
            gameData.awaitingInput = null;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;

            List<Card> chosenCards = new ArrayList<>(gameData.awaitingRevealedHandChosenCards);

            if (discardMode) {
                // Discard chosen cards to graveyard
                List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
                graveyard.addAll(chosenCards);

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                String discardLog = targetName + " discards " + cardNames + ".";
                gameBroadcastService.logAndBroadcast(gameData, discardLog);
                log.info("Game {} - {} discards {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);
            } else {
                // Put chosen cards on top of library
                List<Card> deck = gameData.playerDecks.get(targetPlayerId);

                // Insert in reverse order so first chosen ends up on top
                for (int i = chosenCards.size() - 1; i >= 0; i--) {
                    deck.addFirst(chosenCards.get(i));
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                String putLog = player.getUsername() + " puts " + cardNames + " on top of " + targetName + "'s library.";
                gameBroadcastService.logAndBroadcast(gameData, putLog);
                log.info("Game {} - {} puts {} on top of {}'s library", gameData.id, player.getUsername(), cardNames, targetName);
            }

            gameData.awaitingRevealedHandChoiceTargetPlayerId = null;
            gameData.awaitingRevealedHandChoiceRemainingCount = 0;
            gameData.awaitingRevealedHandChoiceDiscardMode = false;
            gameData.awaitingRevealedHandChosenCards.clear();

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card, UUID targetPermanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            gameData.playerBattlefields.get(playerId).add(auraPerm);

            String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield attached to " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            hand.add(card);
            String logEntry = card.getName() + " can't be attached (target left the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Aura target gone, {} returned to hand", gameData.id, card.getName());
        }
    }

    void resolveUntargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card) {
        gameData.playerBattlefields.get(playerId).add(new Permanent(card));

        String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} onto the battlefield", gameData.id, player.getUsername(), card.getName());

        gameHelper.handleCreatureEnteredBattlefield(gameData, playerId, card, null);
    }

    void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        if (gameData.awaitingInput != AwaitingInput.PERMANENT_CHOICE) {
            throw new IllegalStateException("Not awaiting permanent choice");
        }
        if (!player.getId().equals(gameData.awaitingPermanentChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = gameData.awaitingPermanentChoiceValidIds;

        gameData.awaitingInput = null;
        gameData.awaitingPermanentChoicePlayerId = null;
        gameData.awaitingPermanentChoiceValidIds = null;

        if (!validIds.contains(permanentId)) {
            throw new IllegalStateException("Invalid permanent: " + permanentId);
        }

        PermanentChoiceContext context = gameData.permanentChoiceContext;
        gameData.permanentChoiceContext = null;

        if (context instanceof PermanentChoiceContext.CloneCopy) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, permanentId);
            if (targetPerm == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            gameHelper.completeCloneEntry(gameData, permanentId);

            // If no legend rule or other awaiting input pending, do SBA + auto-pass
            if (gameData.awaitingInput == null) {
                gameHelper.performStateBasedActions(gameData);

                if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                    gameHelper.processNextDeathTriggerTarget(gameData);
                    if (gameData.awaitingInput != null) {
                        return;
                    }
                }

                if (!gameData.pendingMayAbilities.isEmpty()) {
                    playerInputService.processNextMayAbility(gameData);
                    return;
                }

                turnProgressionService.resolveAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.AuraGraft auraGraft) {
            Permanent aura = gameQueryService.findPermanentById(gameData, auraGraft.auraPermanentId());
            if (aura == null) {
                throw new IllegalStateException("Aura permanent no longer exists");
            }

            Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
            if (newTarget == null) {
                throw new IllegalStateException("Target permanent no longer exists");
            }

            aura.setAttachedTo(permanentId);

            String logEntry = aura.getCard().getName() + " is now attached to " + newTarget.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.LegendRule legendRule) {
            // Legend rule: keep chosen permanent, move all others with the same name to graveyard
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            List<Permanent> toRemove = new ArrayList<>();
            for (Permanent perm : battlefield) {
                if (perm.getCard().getName().equals(legendRule.cardName()) && !perm.getId().equals(permanentId)) {
                    toRemove.add(perm);
                }
            }
            for (Permanent perm : toRemove) {
                boolean wasCreature = gameQueryService.isCreature(gameData, perm);
                battlefield.remove(perm);
                gameData.playerGraveyards.get(playerId).add(perm.getOriginalCard());
                gameHelper.collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature);
                if (wasCreature) {
                    gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                }
                String logEntry = perm.getCard().getName() + " is put into the graveyard (legend rule).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
            }

            gameHelper.removeOrphanedAuras(gameData);

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            UUID sacrificingPlayerId = sacrificeCreature.sacrificingPlayerId();
            gameHelper.removePermanentToGraveyard(gameData, target);

            String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
            String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

            gameHelper.performStateBasedActions(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.BounceCreature bounceCreature) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            UUID bouncingPlayerId = bounceCreature.bouncingPlayerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(bouncingPlayerId);
            if (battlefield != null && battlefield.remove(target)) {
                gameHelper.removeOrphanedAuras(gameData);
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), bouncingPlayerId);
                gameData.stolenCreatures.remove(target.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(target.getOriginalCard());

                String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by Sunken Hope", gameData.id, target.getCard().getName());
            }

            gameHelper.performStateBasedActions(gameData);

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.CopySpellRetarget retarget) {
            StackEntry copyEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(retarget.copyCardId())) {
                    copyEntry = se;
                    break;
                }
            }
            if (copyEntry == null) {
                log.info("Game {} - Copy no longer on stack for retarget", gameData.id);
            } else {
                copyEntry.setTargetPermanentId(permanentId);
                String logMsg = "Copy of " + copyEntry.getCard().getName() + " now targets " + getTargetDisplayName(gameData, permanentId) + ".";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - Copy retargeted to {}", gameData.id, getTargetDisplayName(gameData, permanentId));
            }

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.DeathTriggerTarget dtt) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                // Create the triggered ability stack entry with the chosen target
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        dtt.dyingCard(),
                        dtt.controllerId(),
                        dtt.dyingCard().getName() + "'s ability",
                        new ArrayList<>(dtt.effects())
                );
                entry.setTargetPermanentId(permanentId);
                gameData.stack.add(entry);

                String logEntry = dtt.dyingCard().getName() + "'s death trigger targets " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} death trigger targets {}", gameData.id, dtt.dyingCard().getName(), target.getCard().getName());
            } else {
                String logEntry = dtt.dyingCard().getName() + "'s death trigger has no valid target.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} death trigger target no longer exists", gameData.id, dtt.dyingCard().getName());
            }

            // Process more pending death trigger targets
            if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                gameHelper.processNextDeathTriggerTarget(gameData);
                return;
            }

            // Process pending may abilities
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            turnProgressionService.resolveAutoPass(gameData);
        } else if (gameData.pendingAuraCard != null) {
            Card auraCard = gameData.pendingAuraCard;
            gameData.pendingAuraCard = null;

            Permanent creatureTarget = gameQueryService.findPermanentById(gameData, permanentId);
            if (creatureTarget == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            // Create Aura permanent attached to the creature, under controller's control
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(creatureTarget.getId());
            gameData.playerBattlefields.get(playerId).add(auraPerm);

            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = auraCard.getName() + " enters the battlefield from graveyard attached to " + creatureTarget.getCard().getName() + " under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned {} from graveyard to battlefield attached to {}",
                    gameData.id, playerName, auraCard.getName(), creatureTarget.getCard().getName());

            turnProgressionService.resolveAutoPass(gameData);
        } else {
            throw new IllegalStateException("No pending permanent choice context");
        }
    }

    void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.awaitingInput != AwaitingInput.GRAVEYARD_CHOICE) {
            throw new IllegalStateException("Not awaiting graveyard choice");
        }
        if (!player.getId().equals(gameData.awaitingGraveyardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<Integer> validIndices = gameData.awaitingGraveyardChoiceValidIndices;
        List<Card> cardPool = gameData.graveyardChoiceCardPool;

        gameData.awaitingInput = null;
        gameData.awaitingGraveyardChoicePlayerId = null;
        gameData.awaitingGraveyardChoiceValidIndices = null;
        GraveyardChoiceDestination destination = gameData.graveyardChoiceDestination;
        gameData.graveyardChoiceDestination = null;
        gameData.graveyardChoiceCardPool = null;

        if (cardIndex == -1) {
            // Player declined
            String logEntry = player.getUsername() + " chooses not to return a card.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to return a card from graveyard", gameData.id, player.getUsername());
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            Card card;
            if (cardPool != null) {
                // Cross-graveyard choice: card pool contains cards from any graveyard
                card = cardPool.get(cardIndex);
                gameHelper.removeCardFromGraveyardById(gameData, card.getId());
            } else {
                // Standard choice: indices into the player's own graveyard
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                card = graveyard.remove(cardIndex);
            }

            switch (destination) {
                case HAND -> {
                    gameData.playerHands.get(playerId).add(card);

                    String logEntry = player.getUsername() + " returns " + card.getName() + " from graveyard to hand.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, player.getUsername(), card.getName());
                }
                case BATTLEFIELD -> {
                    Permanent perm = new Permanent(card);
                    gameData.playerBattlefields.get(playerId).add(perm);

                    String logEntry = player.getUsername() + " puts " + card.getName() + " from a graveyard onto the battlefield.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} puts {} from graveyard onto battlefield", gameData.id, player.getUsername(), card.getName());

                    if (card.getType() == CardType.CREATURE) {
                        gameHelper.handleCreatureEnteredBattlefield(gameData, playerId, card, null);
                    }
                    if (gameData.awaitingInput == null) {
                        gameHelper.checkLegendRule(gameData, playerId);
                    }
                }
            }
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        if (gameData.awaitingInput != AwaitingInput.MULTI_PERMANENT_CHOICE) {
            throw new IllegalStateException("Not awaiting multi-permanent choice");
        }
        if (!player.getId().equals(gameData.awaitingMultiPermanentChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = gameData.awaitingMultiPermanentChoiceValidIds;
        int maxCount = gameData.awaitingMultiPermanentChoiceMaxCount;

        gameData.awaitingInput = null;
        gameData.awaitingMultiPermanentChoicePlayerId = null;
        gameData.awaitingMultiPermanentChoiceValidIds = null;
        gameData.awaitingMultiPermanentChoiceMaxCount = 0;

        if (permanentIds == null) {
            permanentIds = List.of();
        }

        if (permanentIds.size() > maxCount) {
            throw new IllegalStateException("Too many permanents selected: " + permanentIds.size() + " > " + maxCount);
        }

        // Validate no duplicates
        Set<UUID> uniqueIds = new HashSet<>(permanentIds);
        if (uniqueIds.size() != permanentIds.size()) {
            throw new IllegalStateException("Duplicate permanent IDs in selection");
        }

        for (UUID permId : permanentIds) {
            if (!validIds.contains(permId)) {
                throw new IllegalStateException("Invalid permanent: " + permId);
            }
        }

        if (gameData.pendingCombatDamageBounceTargetPlayerId != null) {
            UUID targetPlayerId = gameData.pendingCombatDamageBounceTargetPlayerId;
            gameData.pendingCombatDamageBounceTargetPlayerId = null;

            if (permanentIds.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to return any permanents.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                List<Permanent> targetBattlefield = gameData.playerBattlefields.get(targetPlayerId);
                List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
                List<String> bouncedNames = new ArrayList<>();

                for (UUID permId : permanentIds) {
                    Permanent toReturn = null;
                    for (Permanent p : targetBattlefield) {
                        if (p.getId().equals(permId)) {
                            toReturn = p;
                            break;
                        }
                    }
                    if (toReturn != null) {
                        targetBattlefield.remove(toReturn);
                        targetHand.add(toReturn.getCard());
                        bouncedNames.add(toReturn.getCard().getName());
                    }
                }

                if (!bouncedNames.isEmpty()) {
                    gameHelper.removeOrphanedAuras(gameData);
                    String logEntry = String.join(", ", bouncedNames) + (bouncedNames.size() == 1 ? " is" : " are") + " returned to " + gameData.playerIdToName.get(targetPlayerId) + "'s hand.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} bounced {} permanents", gameData.id, gameData.playerIdToName.get(playerId), bouncedNames.size());
                }
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            turnProgressionService.advanceStep(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else {
            throw new IllegalStateException("No pending multi-permanent choice context");
        }
    }

    void handleMultipleGraveyardCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        if (gameData.awaitingInput == AwaitingInput.LIBRARY_REVEAL_CHOICE) {
            handleLibraryRevealChoice(gameData, player, cardIds);
            return;
        }
        if (gameData.awaitingInput != AwaitingInput.MULTI_GRAVEYARD_CHOICE) {
            throw new IllegalStateException("Not awaiting multi-graveyard choice");
        }
        if (!player.getId().equals(gameData.awaitingMultiGraveyardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<UUID> validIds = gameData.awaitingMultiGraveyardChoiceValidCardIds;
        int maxCount = gameData.awaitingMultiGraveyardChoiceMaxCount;

        if (cardIds == null) {
            cardIds = List.of();
        }

        if (cardIds.size() > maxCount) {
            throw new IllegalStateException("Too many cards selected: " + cardIds.size() + " > " + maxCount);
        }

        Set<UUID> uniqueIds = new HashSet<>(cardIds);
        if (uniqueIds.size() != cardIds.size()) {
            throw new IllegalStateException("Duplicate card IDs in selection");
        }

        for (UUID cardId : cardIds) {
            if (!validIds.contains(cardId)) {
                throw new IllegalStateException("Invalid card: " + cardId);
            }
        }

        // Retrieve the pending ETB info
        Card pendingCard = gameData.pendingGraveyardTargetCard;
        UUID controllerId = gameData.pendingGraveyardTargetControllerId;
        List<CardEffect> pendingEffects = gameData.pendingGraveyardTargetEffects;

        // Clear awaiting state
        gameData.awaitingInput = null;
        gameData.awaitingMultiGraveyardChoicePlayerId = null;
        gameData.awaitingMultiGraveyardChoiceValidCardIds = null;
        gameData.awaitingMultiGraveyardChoiceMaxCount = 0;
        gameData.pendingGraveyardTargetCard = null;
        gameData.pendingGraveyardTargetControllerId = null;
        gameData.pendingGraveyardTargetEffects = null;

        // Put the ETB ability on the stack with the chosen targets
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                pendingCard,
                controllerId,
                pendingCard.getName() + "'s ETB ability",
                new ArrayList<>(pendingEffects),
                new ArrayList<>(cardIds)
        ));

        if (cardIds.isEmpty()) {
            String etbLog = pendingCard.getName() + "'s enter-the-battlefield ability triggers targeting no cards.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
        } else {
            List<String> targetNames = new ArrayList<>();
            for (UUID cardId : cardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    targetNames.add(card.getName());
                }
            }
            String etbLog = pendingCard.getName() + "'s enter-the-battlefield ability triggers targeting " + String.join(", ", targetNames) + ".";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
        }
        log.info("Game {} - {} ETB ability pushed onto stack with {} graveyard targets", gameData.id, pendingCard.getName(), cardIds.size());

        turnProgressionService.resolveAutoPass(gameData);
    }

    void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        if (gameData.awaitingInput != AwaitingInput.MAY_ABILITY_CHOICE) {
            throw new IllegalStateException("Not awaiting may ability choice");
        }
        if (!player.getId().equals(gameData.awaitingMayAbilityPlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        PendingMayAbility ability = gameData.pendingMayAbilities.removeFirst();
        gameData.awaitingInput = null;
        gameData.awaitingMayAbilityPlayerId = null;

        // Counter-unless-pays — handled via the may ability system
        boolean isCounterUnlessPays = ability.effects().stream().anyMatch(e -> e instanceof CounterUnlessPaysEffect);
        if (isCounterUnlessPays) {
            handleCounterUnlessPaysChoice(gameData, player, accepted, ability);
            return;
        }

        // Sacrifice-unless-discard — handled via the may ability system
        boolean isSacrificeUnlessDiscard = ability.effects().stream().anyMatch(e -> e instanceof SacrificeUnlessDiscardCardTypeEffect);
        if (isSacrificeUnlessDiscard) {
            handleSacrificeUnlessDiscardChoice(gameData, player, accepted, ability);
            return;
        }

        // Copy spell retarget — choose new targets for a copied spell
        boolean isCopySpellRetarget = ability.effects().stream().anyMatch(e -> e instanceof CopySpellEffect);
        if (isCopySpellRetarget) {
            handleCopySpellRetargetChoice(gameData, player, accepted, ability);
            return;
        }

        // Clone copy creature effect — handled as replacement effect (pre-entry)
        boolean isCloneCopy = ability.effects().stream().anyMatch(e -> e instanceof CopyCreatureOnEnterEffect);
        if (isCloneCopy) {
            if (accepted) {
                // Collect valid creature targets (Clone is NOT on the battlefield yet)
                List<UUID> creatureIds = new ArrayList<>();
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (gameQueryService.isCreature(gameData, p)) {
                            creatureIds.add(p.getId());
                        }
                    }
                }
                playerInputService.beginPermanentChoice(gameData, ability.controllerId(), creatureIds, "Choose a creature to copy.");

                String logEntry = player.getUsername() + " accepts — choosing a creature to copy.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts clone copy", gameData.id, player.getUsername());
            } else {
                gameData.permanentChoiceContext = null;
                String logEntry = player.getUsername() + " declines to copy a creature. Clone enters as 0/0.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines clone copy", gameData.id, player.getUsername());

                gameHelper.completeCloneEntry(gameData, null);
                gameHelper.performStateBasedActions(gameData);

                if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                    gameHelper.processNextDeathTriggerTarget(gameData);
                    if (gameData.awaitingInput != null) {
                        return;
                    }
                }

                if (!gameData.pendingMayAbilities.isEmpty()) {
                    playerInputService.processNextMayAbility(gameData);
                    return;
                }

                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (accepted) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(ability.effects())
            ));

            String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName() + "'s triggered ability goes on the stack.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            String logEntry = player.getUsername() + " declines " + ability.sourceCard().getName() + "'s triggered ability.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        playerInputService.processNextMayAbility(gameData);

        if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    void handleCounterUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        int amount = ability.effects().stream()
                .filter(e -> e instanceof CounterUnlessPaysEffect)
                .map(e -> ((CounterUnlessPaysEffect) e).amount())
                .findFirst().orElse(0);
        UUID targetCardId = ability.targetCardId();

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter-unless-pays target no longer on stack", gameData.id);
            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (accepted) {
            ManaCost cost = new ManaCost("{" + amount + "}");
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            if (cost.canPay(pool)) {
                cost.pay(pool);
                String logEntry = player.getUsername() + " pays {" + amount + "}. " + targetEntry.getCard().getName() + " is not countered.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} pays {} to avoid counter", gameData.id, player.getUsername(), amount);
            } else {
                gameData.stack.remove(targetEntry);
                gameData.playerGraveyards.get(targetEntry.getControllerId()).add(targetEntry.getCard());
                String logEntry = player.getUsername() + " can't pay {" + amount + "}. " + targetEntry.getCard().getName() + " is countered.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} can't pay {} — spell countered", gameData.id, player.getUsername(), amount);
            }
        } else {
            gameData.stack.remove(targetEntry);
            gameData.playerGraveyards.get(targetEntry.getControllerId()).add(targetEntry.getCard());
            String logEntry = player.getUsername() + " declines to pay {" + amount + "}. " + targetEntry.getCard().getName() + " is countered.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to pay {} — spell countered", gameData.id, player.getUsername(), amount);
        }

        gameHelper.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    void handleSacrificeUnlessDiscardChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        SacrificeUnlessDiscardCardTypeEffect effect = ability.effects().stream()
                .filter(e -> e instanceof SacrificeUnlessDiscardCardTypeEffect)
                .map(e -> (SacrificeUnlessDiscardCardTypeEffect) e)
                .findFirst().orElseThrow();

        Card sourceCard = ability.sourceCard();
        UUID controllerId = ability.controllerId();

        // Find the source permanent on the battlefield
        Permanent sourcePermanent = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                    break;
                }
            }
        }

        if (accepted) {
            // Per ruling 2008-04-01: player may still discard even if the creature
            // is no longer on the battlefield.
            List<Card> hand = gameData.playerHands.get(controllerId);
            List<Integer> validIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    if (hand.get(i).getType() == effect.requiredType()) {
                        validIndices.add(i);
                    }
                }
            }

            if (!validIndices.isEmpty()) {
                String typeName = effect.requiredType().name().toLowerCase();
                gameData.awaitingDiscardRemainingCount = 1;
                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose a " + typeName + " card to discard.");

                String logEntry = player.getUsername() + " chooses to discard a " + typeName + " card.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts sacrifice-unless-discard for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Hand changed since trigger — no valid cards left, fall through to sacrifice
        }

        // Declined or no valid cards left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            gameHelper.removePermanentToGraveyard(gameData, sourcePermanent);
            String logEntry = player.getUsername() + " declines to discard. " + sourceCard.getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        gameHelper.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    void handleCopySpellRetargetChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            String logEntry = player.getUsername() + " keeps the original targets for the copy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to retarget copy", gameData.id, player.getUsername());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        // Find the copy on the stack
        UUID copyCardId = ability.targetCardId();
        StackEntry copyEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(copyCardId)) {
                copyEntry = se;
                break;
            }
        }

        if (copyEntry == null) {
            log.info("Game {} - Copy no longer on stack for retarget", gameData.id);
            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        Card copiedCard = copyEntry.getCard();
        List<UUID> validTargets = new ArrayList<>();

        if (copiedCard.isNeedsSpellTarget()) {
            // Targets a spell on the stack
            SpellTypeTargetFilter spellFilter = copiedCard.getTargetFilter() instanceof SpellTypeTargetFilter stf ? stf : null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(copyCardId)) continue; // exclude the copy itself
                if (spellFilter != null && !spellFilter.spellTypes().contains(se.getEntryType())) continue;
                validTargets.add(se.getCard().getId());
            }
        } else if (copiedCard.isNeedsTarget()) {
            List<CardEffect> effects = copyEntry.getEffectsToResolve();

            // Check if it targets a player only
            boolean targetsPlayer = isPlayerTargetingEffects(effects);
            if (targetsPlayer) {
                validTargets.addAll(gameData.orderedPlayerIds);
            } else {
                // Determine targeting category from effects
                boolean targetsAnyPermanent = false;
                boolean targetsEnchantmentOnly = false;
                boolean targetsCreatureOrPlayer = false;
                boolean requiresAttacking = false;
                Set<CardType> configuredTargetTypes = null;

                for (CardEffect effect : effects) {
                    if (effect instanceof ReturnTargetPermanentToHandEffect) {
                        targetsAnyPermanent = true;
                        break;
                    }
                    if (effect instanceof GainControlOfTargetAuraEffect) {
                        targetsEnchantmentOnly = true;
                        break;
                    }
                    if (effect instanceof DealXDamageToAnyTargetAndGainXLifeEffect
                            || effect instanceof PreventDamageToTargetEffect) {
                        targetsCreatureOrPlayer = true;
                        break;
                    }
                    if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                        requiresAttacking = true;
                        break;
                    }
                    if (effect instanceof DestroyTargetPermanentEffect dte) {
                        configuredTargetTypes = dte.targetTypes();
                        break;
                    }
                    if (effect instanceof TapOrUntapTargetPermanentEffect toue) {
                        configuredTargetTypes = toue.allowedTypes();
                        break;
                    }
                    if (effect instanceof TapTargetPermanentEffect tpe) {
                        configuredTargetTypes = tpe.allowedTypes();
                        break;
                    }
                }

                // "Any target" spells can also target players
                if (targetsCreatureOrPlayer) {
                    validTargets.addAll(gameData.orderedPlayerIds);
                }

                // Add matching permanents from all battlefields
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (targetsAnyPermanent) {
                            validTargets.add(p.getId());
                        } else if (targetsEnchantmentOnly) {
                            if (p.getCard().getType() == CardType.ENCHANTMENT && p.getAttachedTo() != null) {
                                validTargets.add(p.getId());
                            }
                        } else if (configuredTargetTypes != null) {
                            if (configuredTargetTypes.contains(p.getCard().getType())) {
                                validTargets.add(p.getId());
                            }
                        } else if (requiresAttacking) {
                            if (gameQueryService.isCreature(gameData, p) && p.isAttacking()) {
                                validTargets.add(p.getId());
                            }
                        } else {
                            // Default: creature targeting (including "any target" which also adds players above)
                            if (gameQueryService.isCreature(gameData, p)) {
                                validTargets.add(p.getId());
                            }
                        }
                    }
                }
            }
        }

        if (validTargets.isEmpty()) {
            String logEntry = "No valid targets available for the copy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - No valid targets for copy retarget", gameData.id);

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        gameData.permanentChoiceContext = new PermanentChoiceContext.CopySpellRetarget(copyCardId);
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                "Choose a new target for the copy of " + copiedCard.getName() + ".");
    }

    private boolean isPlayerTargetingEffects(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof ChooseCardsFromTargetHandToTopOfLibraryEffect
                    || effect instanceof DoubleTargetPlayerLifeEffect
                    || effect instanceof ExtraTurnEffect
                    || effect instanceof LookAtHandEffect
                    || effect instanceof MillTargetPlayerEffect
                    || effect instanceof ReturnArtifactsTargetPlayerOwnsToHandEffect
                    || effect instanceof ShuffleGraveyardIntoLibraryEffect
                    || effect instanceof TargetPlayerLosesLifeAndControllerGainsLifeEffect) {
                return true;
            }
        }
        return false;
    }

    String getTargetDisplayName(GameData gameData, UUID targetId) {
        // Check if it's a player
        String playerName = gameData.playerIdToName.get(targetId);
        if (playerName != null) return playerName;

        // Check stack entries
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetId)) return se.getCard().getName();
        }

        // Check battlefield permanents
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(targetId)) return p.getCard().getName();
            }
        }

        return targetId.toString();
    }

    void handleLibraryCardsReordered(GameData gameData, Player player, List<Integer> cardOrder) {
        if (gameData.awaitingInput != AwaitingInput.LIBRARY_REORDER) {
            throw new IllegalStateException("Not awaiting library reorder");
        }
        if (!player.getId().equals(gameData.awaitingLibraryReorderPlayerId)) {
            throw new IllegalStateException("Not your turn to reorder");
        }

        List<Card> reorderCards = gameData.awaitingLibraryReorderCards;
        int count = reorderCards.size();

        if (cardOrder.size() != count) {
            throw new IllegalStateException("Must specify order for all " + count + " cards");
        }

        // Validate that cardOrder is a permutation of 0..count-1
        Set<Integer> seen = new HashSet<>();
        for (int idx : cardOrder) {
            if (idx < 0 || idx >= count) {
                throw new IllegalStateException("Invalid card index: " + idx);
            }
            if (!seen.add(idx)) {
                throw new IllegalStateException("Duplicate card index: " + idx);
            }
        }

        // Apply the reorder: replace top N cards of deck with the reordered ones
        List<Card> deck = gameData.playerDecks.get(player.getId());
        for (int i = 0; i < count; i++) {
            deck.set(i, reorderCards.get(cardOrder.get(i)));
        }

        // Clear awaiting state
        gameData.awaitingInput = null;
        gameData.awaitingLibraryReorderPlayerId = null;
        gameData.awaitingLibraryReorderCards = null;

        String logMsg = player.getUsername() + " puts " + count + " cards back on top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} reordered top {} cards", gameData.id, player.getUsername(), count);

        turnProgressionService.resolveAutoPass(gameData);
    }

    void handleHandTopBottomChosen(GameData gameData, Player player, int handCardIndex, int topCardIndex) {
        if (gameData.awaitingInput != AwaitingInput.HAND_TOP_BOTTOM_CHOICE) {
            throw new IllegalStateException("Not awaiting hand/top/bottom choice");
        }
        if (!player.getId().equals(gameData.awaitingHandTopBottomPlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Card> handTopBottomCards = gameData.awaitingHandTopBottomCards;
        int count = handTopBottomCards.size();

        if (handCardIndex < 0 || handCardIndex >= count) {
            throw new IllegalStateException("Invalid hand card index: " + handCardIndex);
        }
        if (topCardIndex < 0 || topCardIndex >= count) {
            throw new IllegalStateException("Invalid top card index: " + topCardIndex);
        }
        if (handCardIndex == topCardIndex) {
            throw new IllegalStateException("Hand and top card indices must be different");
        }

        UUID playerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(playerId);

        // Put the chosen card into hand
        Card handCard = handTopBottomCards.get(handCardIndex);
        gameData.playerHands.get(playerId).add(handCard);

        // Put the chosen card on top of library
        Card topCard = handTopBottomCards.get(topCardIndex);
        deck.add(0, topCard);

        // Put the remaining card on the bottom of library
        for (int i = 0; i < count; i++) {
            if (i != handCardIndex && i != topCardIndex) {
                deck.add(handTopBottomCards.get(i));
            }
        }

        // Clear awaiting state
        gameData.awaitingInput = null;
        gameData.awaitingHandTopBottomPlayerId = null;
        gameData.awaitingHandTopBottomCards = null;

        String logMsg;
        if (count == 2) {
            logMsg = player.getUsername() + " puts one card into their hand and one on top of their library.";
        } else {
            logMsg = player.getUsername() + " puts one card into their hand, one on top of their library, and one on the bottom.";
        }
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} completed hand/top/bottom choice", gameData.id, player.getUsername());

        turnProgressionService.resolveAutoPass(gameData);
    }

    void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.awaitingInput != AwaitingInput.LIBRARY_SEARCH) {
            throw new IllegalStateException("Not awaiting library search");
        }
        if (!player.getId().equals(gameData.awaitingLibrarySearchPlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        List<Card> searchCards = gameData.awaitingLibrarySearchCards;

        boolean reveals = gameData.awaitingLibrarySearchReveals;
        boolean canFailToFind = gameData.awaitingLibrarySearchCanFailToFind;
        UUID targetPlayerId = gameData.awaitingLibrarySearchTargetPlayerId;
        int remainingCount = gameData.awaitingLibrarySearchRemainingCount;

        // Determine whose library/hand to use
        UUID deckOwnerId = targetPlayerId != null ? targetPlayerId : playerId;
        UUID handOwnerId = targetPlayerId != null ? targetPlayerId : playerId;

        // Clear all state
        gameData.awaitingInput = null;
        gameData.awaitingLibrarySearchPlayerId = null;
        gameData.awaitingLibrarySearchCards = null;
        gameData.awaitingLibrarySearchReveals = false;
        gameData.awaitingLibrarySearchCanFailToFind = false;
        gameData.awaitingLibrarySearchTargetPlayerId = null;
        gameData.awaitingLibrarySearchRemainingCount = 0;

        List<Card> deck = gameData.playerDecks.get(deckOwnerId);

        if (cardIndex == -1) {
            // Player declined (fail to find) — only allowed for restricted searches (e.g. basic land)
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
            Collections.shuffle(deck);
            String logEntry = player.getUsername() + " chooses not to take a card. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to take a card from library", gameData.id, player.getUsername());
        } else {
            if (cardIndex < 0 || cardIndex >= searchCards.size()) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            Card chosenCard = searchCards.get(cardIndex);

            // Remove the chosen card from the library by identity
            boolean removed = false;
            for (int i = 0; i < deck.size(); i++) {
                if (deck.get(i).getId().equals(chosenCard.getId())) {
                    deck.remove(i);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                throw new IllegalStateException("Chosen card not found in library");
            }

            gameData.playerHands.get(handOwnerId).add(chosenCard);

            // Head Games multi-pick: more cards to choose
            if (targetPlayerId != null && remainingCount > 1) {
                int newRemaining = remainingCount - 1;
                List<Card> newSearchCards = new ArrayList<>(deck);

                gameData.awaitingInput = AwaitingInput.LIBRARY_SEARCH;
                gameData.awaitingLibrarySearchPlayerId = playerId;
                gameData.awaitingLibrarySearchCards = newSearchCards;
                gameData.awaitingLibrarySearchReveals = false;
                gameData.awaitingLibrarySearchCanFailToFind = false;
                gameData.awaitingLibrarySearchTargetPlayerId = targetPlayerId;
                gameData.awaitingLibrarySearchRemainingCount = newRemaining;

                String targetName = gameData.playerIdToName.get(targetPlayerId);
                List<CardView> cardViews = newSearchCards.stream().map(cardViewFactory::create).toList();
                sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(
                        cardViews,
                        "Search " + targetName + "'s library for a card to put into their hand (" + newRemaining + " remaining).",
                        false
                ));

                log.info("Game {} - {} picks for Head Games, {} remaining", gameData.id, player.getUsername(), newRemaining);
                return;
            }

            Collections.shuffle(deck);

            String logEntry;
            if (targetPlayerId != null) {
                String targetName = gameData.playerIdToName.get(targetPlayerId);
                logEntry = player.getUsername() + " puts cards into " + targetName + "'s hand. " + targetName + "'s library is shuffled.";
            } else if (reveals) {
                logEntry = player.getUsername() + " reveals " + chosenCard.getName() + " and puts it into their hand. Library is shuffled.";
            } else {
                logEntry = player.getUsername() + " puts a card into their hand. Library is shuffled.";
            }
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} searches library and puts {} into hand", gameData.id, player.getUsername(), chosenCard.getName());
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleLibraryRevealChoice(GameData gameData, Player player, List<UUID> cardIds) {
        if (!player.getId().equals(gameData.awaitingLibraryRevealPlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<UUID> validIds = gameData.awaitingLibraryRevealValidCardIds;
        if (cardIds == null) {
            cardIds = List.of();
        }

        for (UUID cardId : cardIds) {
            if (!validIds.contains(cardId)) {
                throw new IllegalStateException("Invalid card: " + cardId);
            }
        }

        Set<UUID> uniqueIds = new HashSet<>(cardIds);
        if (uniqueIds.size() != cardIds.size()) {
            throw new IllegalStateException("Duplicate card IDs in selection");
        }

        UUID controllerId = gameData.awaitingLibraryRevealPlayerId;
        List<Card> allRevealedCards = gameData.awaitingLibraryRevealAllCards;
        String playerName = gameData.playerIdToName.get(controllerId);

        // Clear awaiting state
        gameData.awaitingInput = null;
        gameData.awaitingLibraryRevealPlayerId = null;
        gameData.awaitingLibraryRevealValidCardIds = null;
        gameData.awaitingLibraryRevealAllCards = null;

        // Separate selected cards from the rest
        Set<UUID> selectedIds = new HashSet<>(cardIds);
        List<Card> selectedCards = new ArrayList<>();
        List<Card> remainingCards = new ArrayList<>();
        for (Card card : allRevealedCards) {
            if (selectedIds.contains(card.getId())) {
                selectedCards.add(card);
            } else {
                remainingCards.add(card);
            }
        }

        // Put selected cards onto the battlefield
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Card card : selectedCards) {
            Permanent perm = new Permanent(card);
            battlefield.add(perm);

            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            if (card.getType() == CardType.CREATURE) {
                gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, card, null);
            }
            if (card.getType() == CardType.PLANESWALKER && card.getLoyalty() != null) {
                perm.setLoyaltyCounters(card.getLoyalty());
                perm.setSummoningSick(false);
            }
            if (gameData.awaitingInput == null) {
                gameHelper.checkLegendRule(gameData, controllerId);
            }
        }

        // Shuffle remaining cards back into library
        List<Card> deck = gameData.playerDecks.get(controllerId);
        deck.addAll(remainingCards);
        Collections.shuffle(deck);

        if (selectedCards.isEmpty()) {
            String logEntry = playerName + " puts no cards onto the battlefield. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            String names = selectedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            String logEntry = playerName + " puts " + names + " onto the battlefield. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} resolves library reveal choice, {} cards to battlefield", gameData.id, playerName, selectedCards.size());

        gameHelper.performStateBasedActions(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }
}
