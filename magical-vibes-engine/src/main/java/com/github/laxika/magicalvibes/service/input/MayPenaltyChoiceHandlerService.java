package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.EachPlayerTakesDamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayPenaltyChoiceHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerTakesDamageUnlessPaysEffectHandler eachPlayerTakesDamageUnlessPaysEffectHandler;
    private final ResolutionCostPaymentService resolutionCostPaymentService;

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
