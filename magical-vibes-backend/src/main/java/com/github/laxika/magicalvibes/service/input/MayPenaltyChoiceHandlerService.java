package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayPenaltyChoiceHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final StateBasedActionService stateBasedActionService;
    private final PermanentRemovalService permanentRemovalService;

    public void handleCounterUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
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
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameQueryService.isProtectedFromCounterBySourceCard(gameData, targetEntry.getControllerId(), ability.sourceCard())) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    ability.sourceCard().getColor().name().toLowerCase());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
                graveyardService.addCardToGraveyard(gameData, targetEntry.getControllerId(), targetEntry.getCard());
                String logEntry = player.getUsername() + " can't pay {" + amount + "}. " + targetEntry.getCard().getName() + " is countered.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} can't pay {} — spell countered", gameData.id, player.getUsername(), amount);
            }
        } else {
            gameData.stack.remove(targetEntry);
            graveyardService.addCardToGraveyard(gameData, targetEntry.getControllerId(), targetEntry.getCard());
            String logEntry = player.getUsername() + " declines to pay {" + amount + "}. " + targetEntry.getCard().getName() + " is countered.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to pay {} — spell countered", gameData.id, player.getUsername(), amount);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeUnlessDiscardChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
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
                    if (effect.requiredType() == null || hand.get(i).getType() == effect.requiredType()) {
                        validIndices.add(i);
                    }
                }
            }

            if (!validIndices.isEmpty()) {
                String typeName = effect.requiredType() == null ? "card" : effect.requiredType().name().toLowerCase() + " card";
                gameData.discardCausedByOpponent = false;
                gameData.interaction.setDiscardRemainingCount(1);
                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose a " + typeName + " to discard.");

                String logEntry = player.getUsername() + " chooses to discard a " + typeName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts sacrifice-unless-discard for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Hand changed since trigger — no valid cards left, fall through to sacrifice
        }

        // Declined or no valid cards left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            String logEntry = player.getUsername() + " declines to discard. " + sourceCard.getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleLoseLifeUnlessDiscardChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LoseLifeUnlessDiscardEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LoseLifeUnlessDiscardEffect)
                .map(e -> (LoseLifeUnlessDiscardEffect) e)
                .findFirst().orElseThrow();

        UUID targetPlayerId = ability.controllerId();

        if (accepted) {
            List<Card> hand = gameData.playerHands.get(targetPlayerId);
            List<Integer> validIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    validIndices.add(i);
                }
            }

            if (!validIndices.isEmpty()) {
                gameData.discardCausedByOpponent = false;
                gameData.interaction.setDiscardRemainingCount(1);
                playerInputService.beginDiscardChoice(gameData, targetPlayerId, validIndices,
                        "Choose a card to discard.");

                String logEntry = player.getUsername() + " chooses to discard a card.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts lose-life-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to life loss
        }

        // Declined or no cards — lose life
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + "'s life total can't change.");
        } else {
            int currentLife = gameData.getLife(targetPlayerId);
            gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());

            String logEntry = player.getUsername() + " loses " + effect.lifeLoss() + " life. (" + ability.sourceCard().getName() + ")";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} loses {} life (declined discard, {})", gameData.id, player.getUsername(), effect.lifeLoss(), ability.sourceCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleLoseLifeUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LoseLifeUnlessPaysEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LoseLifeUnlessPaysEffect)
                .map(e -> (LoseLifeUnlessPaysEffect) e)
                .findFirst().orElseThrow();

        UUID targetPlayerId = ability.controllerId();

        if (accepted) {
            ManaCost cost = new ManaCost("{" + effect.payAmount() + "}");
            ManaPool pool = gameData.playerManaPools.get(targetPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                String logEntry = player.getUsername() + " pays {" + effect.payAmount() + "}. (" + ability.sourceCard().getName() + ")";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} pays {} to avoid life loss ({})", gameData.id, player.getUsername(), effect.payAmount(), ability.sourceCard().getName());
            } else {
                // Can't pay — apply life loss
                if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                    gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + "'s life total can't change.");
                } else {
                    int currentLife = gameData.getLife(targetPlayerId);
                    gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());
                    String logEntry = player.getUsername() + " can't pay {" + effect.payAmount() + "}. " + player.getUsername() + " loses " + effect.lifeLoss() + " life. (" + ability.sourceCard().getName() + ")";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} can't pay {} — loses {} life ({})", gameData.id, player.getUsername(), effect.payAmount(), effect.lifeLoss(), ability.sourceCard().getName());
                }
            }
        } else {
            // Declined — lose life
            if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(targetPlayerId);
                gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());
                String logEntry = player.getUsername() + " loses " + effect.lifeLoss() + " life. (" + ability.sourceCard().getName() + ")";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} loses {} life (declined to pay, {})", gameData.id, player.getUsername(), effect.lifeLoss(), ability.sourceCard().getName());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleOpponentExileChoice(GameData gameData, Player player, boolean accepted,
                                           PendingMayAbility ability, OpponentMayReturnExiledCardOrDrawEffect effect) {
        UUID opponentId = ability.controllerId(); // opponent is the decision maker
        UUID exiledCardId = ability.targetCardId();

        // Find the spell controller (the other player)
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(opponentId)) {
                controllerId = pid;
                break;
            }
        }

        if (controllerId == null) {
            throw new IllegalStateException("Cannot find exiled card owner");
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        if (accepted) {
            // Opponent lets the controller have the exiled card — move from exile to hand
            Card exiledCard = null;
            List<Card> exileZone = gameData.playerExiledCards.get(controllerId);
            if (exileZone != null) {
                for (int i = 0; i < exileZone.size(); i++) {
                    if (exileZone.get(i).getId().equals(exiledCardId)) {
                        exiledCard = exileZone.remove(i);
                        break;
                    }
                }
            }

            if (exiledCard != null) {
                gameData.addCardToHand(controllerId, exiledCard);
                String logEntry = opponentName + " allows it. " + controllerName + " puts " + exiledCard.getName() + " into their hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} allows exile return, {} gets {}", gameData.id, opponentName, controllerName, exiledCard.getName());
            }
        } else {
            // Opponent declines — controller draws cards
            int drawCount = effect.drawCount();
            for (int i = 0; i < drawCount; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }

            String logEntry = opponentName + " declines. " + controllerName + " draws " + drawCount + " cards.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines exile return, {} draws {}", gameData.id, opponentName, controllerName, drawCount);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeUnlessReturnOwnPermanentChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect = ability.effects().stream()
                .filter(e -> e instanceof SacrificeUnlessReturnOwnPermanentTypeToHandEffect)
                .map(e -> (SacrificeUnlessReturnOwnPermanentTypeToHandEffect) e)
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
            // Collect valid permanent IDs of the required type
            List<UUID> validIds = new ArrayList<>();
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard().hasType(effect.permanentType())) {
                        validIds.add(p.getId());
                    }
                }
            }

            if (!validIds.isEmpty()) {
                String typeName = effect.permanentType().name().toLowerCase();
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.BounceOwnPermanentOrSacrificeSelf(controllerId, sourceCard.getId()));
                playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                        "Choose an " + typeName + " to return to hand.");

                String logEntry = player.getUsername() + " chooses to return an " + typeName + " to hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts sacrifice-unless-return for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Battlefield changed since trigger — no valid permanents left, fall through to sacrifice
        }

        // Declined or no valid permanents left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            String logEntry = player.getUsername() + " declines to return a permanent. " + sourceCard.getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to return a permanent.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
