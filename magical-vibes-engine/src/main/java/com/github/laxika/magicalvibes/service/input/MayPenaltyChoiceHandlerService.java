package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.EachPlayerTakesDamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandDiscardMatchingCardsUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayPenaltyChoiceHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerTakesDamageUnlessPaysEffectHandler eachPlayerTakesDamageUnlessPaysEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.RevealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler revealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler;
    private final LifeSupport lifeSupport;
    private final ResolutionCostPaymentService resolutionCostPaymentService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    /**
     * Runs {@code payment}, the completion of {@code player} accepting {@code ability}, whose life payments
     * ({@code lifePayments}, each payer's total) change nothing before them but mana payments, which can't change what
     * the Lich's Duel Mastery sacrifice a payment causes can be replaced with (paying life is losing it, CR 119.4). That
     * sacrifice's CR 616.1 questions are asked before anything is paid, and the answer is completed again with them
     * once chosen; the window is then closed, so nothing after the questions happens twice.
     */
    private void payLifeAskingDeathReplacementsFirst(GameData gameData, Player player, PendingMayAbility ability,
                                                     Map<UUID, Integer> lifePayments, Runnable payment) {
        permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                gameData, player, ability, true, () -> {
                    lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, lifePayments);
                    payment.run();
                });
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
                gameData.discardCausedByOpponent = ability.sourceControllerId() != null
                        && !ability.sourceControllerId().equals(targetPlayerId);
                playerInputService.beginDiscardChoice(gameData, targetPlayerId, validIndices,
                        "Choose a card to discard.", 1);

                String logEntry = player.getUsername() + " chooses to discard a card.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts lose-life-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to life loss
        }

        // Declined or no cards — lose life, through LifeSupport like every other life loss.
        lifeSupport.applyLifeLoss(gameData, targetPlayerId, effect.lifeLoss(), ability.sourceCard().getName());
        log.info("Game {} - {} declined to discard ({})", gameData.id, player.getUsername(), ability.sourceCard().getName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Lim-Dûl's Hex: each player independently may pay {@code manaCost} or take damage. Accepting
     * spends the mana; declining (or accepting without enough mana) damages that player. Either
     * way, the next remaining player is then offered the same choice (APNAP).
     */
    public void handleEachPlayerTakesDamageUnlessPaysChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability, EachPlayerTakesDamageUnlessPaysEffect effect) {
        UUID targetPlayerId = ability.controllerId();
        boolean paid = false;
        if (accepted) {
            // The prompt carries the effect's controller as its target (EachPlayerTakesDamageUnlessPaysEffectHandler).
            if (resolutionCostPaymentService.payManaFromPool(gameData, targetPlayerId,
                    new ManaCost(effect.manaCost()), ability.targetCardId())) {
                paid = true;
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + effect.manaCost() + ". (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to avoid damage ({})", gameData.id, player.getUsername(),
                        effect.manaCost(), ability.sourceCard().getName());
            }
        }

        eachPlayerTakesDamageUnlessPaysEffectHandler.afterPlayerDecision(
                gameData, ability, effect, targetPlayerId, paid);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Sirocco: one pay-or-discard decision per matching revealed card. Accepting pays the life and
     * keeps that card; declining (or no longer being able to pay) discards it. Either way, the next
     * queued card is then offered.
     */
    public void handleRevealHandDiscardUnlessPaysLifeChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability,
            RevealHandDiscardMatchingCardsUnlessPaysLifeEffect effect) {
        UUID targetPlayerId = ability.controllerId();

        boolean canPay = resolutionCostPaymentService.canPayLife(gameData, targetPlayerId, effect.lifeCost());
        if (accepted && canPay) {
            payLifeAskingDeathReplacementsFirst(gameData, player, ability, Map.of(targetPlayerId, effect.lifeCost()),
                    () -> {
                        lifeSupport.applyLifePayment(gameData, targetPlayerId, effect.lifeCost(),
                                ability.sourceCard().getName());
                        revealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler.afterCardDecision(
                                gameData, ability, effect, targetPlayerId, true);
                        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    });
            return;
        }

        revealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler.afterCardDecision(
                gameData, ability, effect, targetPlayerId, false);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Asks {@code opponentId} whether to let {@code effect}'s controller put {@code exiledCard} into their hand ("any
     * opponent may", Distant Memories).
     */
    public static void offerOpponentExileChoice(GameData gameData, Card exiledCard, UUID opponentId,
                                                OpponentMayReturnExiledCardOrDrawEffect effect) {
        String controllerName = gameData.playerIdToName.get(effect.controllerId());
        String prompt = "Let " + controllerName + " put the exiled card into their hand? If no opponent does, they draw "
                + effect.drawCount() + " cards.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                exiledCard, opponentId, List.of(effect), prompt, exiledCard.getId()));
    }

    public void handleOpponentExileChoice(GameData gameData, Player player, boolean accepted,
                                           PendingMayAbility ability, OpponentMayReturnExiledCardOrDrawEffect effect) {
        UUID opponentId = ability.controllerId(); // opponent is the decision maker
        UUID exiledCardId = ability.targetCardId();
        UUID controllerId = effect.controllerId();

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        if (!accepted) {
            // "Any opponent may": the next opponent in turn order decides; the controller draws only if none does
            // (Distant Memories ruling 2011-06-01).
            List<UUID> opponents = com.github.laxika.magicalvibes.service.effect.SubjectResolutionService
                    .apnapOpponents(gameData, controllerId);
            int next = opponents.indexOf(opponentId) + 1;
            ExiledCardEntry stillExiled = gameData.findExiledCard(exiledCardId);
            if (next > 0 && next < opponents.size() && stillExiled != null) {
                gameLogService.append(gameData, GameLog.text(opponentName + " declines."));
                offerOpponentExileChoice(gameData, stillExiled.card(), opponents.get(next), effect);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        if (accepted) {
            // Opponent lets the controller have the exiled card — move from exile to hand
            Card exiledCard = null;
            ExiledCardEntry exileEntry = gameData.findExiledCard(exiledCardId);
            if (exileEntry != null) {
                exiledCard = exileEntry.card();
                gameData.removeFromExile(exiledCardId);
            }

            if (exiledCard != null) {
                gameData.addCardToHand(controllerId, exiledCard);
                gameLogService.append(gameData, GameLog.textCardText(
                        opponentName + " allows it. " + controllerName + " puts ", exiledCard, " into their hand."));
                log.info("Game {} - {} allows exile return, {} gets {}", gameData.id, opponentName, controllerName, exiledCard.getName());
            }
        } else {
            // Opponent declines — controller draws cards
            int drawCount = effect.drawCount();
            for (int i = 0; i < drawCount; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }

            String logEntry = opponentName + " declines. " + controllerName + " draws " + drawCount + " cards.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines exile return, {} draws {}", gameData.id, opponentName, controllerName, drawCount);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
