package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RiskyMoveEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiskyMoveEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final CoinFlipService coinFlipService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RiskyMoveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (!isPlayer(gameData, controllerId)) {
            return;
        }

        List<UUID> creatureIds = controlledCreatureIds(gameData, controllerId);
        List<UUID> opponentIds = opponentIds(gameData, controllerId);
        if (creatureIds.isEmpty() || opponentIds.isEmpty()) {
            return;
        }

        if (creatureIds.size() == 1) {
            continueAfterCreatureChoice(gameData, entry.getCard(), entry.getSourcePermanentId(),
                    controllerId, creatureIds.getFirst(), opponentIds);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.RiskyMoveCreatureChoice(
                entry.getCard(), entry.getSourcePermanentId(), controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " — choose a creature you control.");
    }

    public void completeCreatureChoice(GameData gameData, UUID creatureId,
                                       PermanentChoiceContext.RiskyMoveCreatureChoice context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, creatureId))
                || !gameQueryService.isCreature(gameData, creature)) {
            throw new IllegalStateException("Chosen creature is no longer controlled by the chooser");
        }

        List<UUID> opponentIds = opponentIds(gameData, context.controllerId());
        if (opponentIds.isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else {
            continueAfterCreatureChoice(gameData, context.sourceCard(), context.sourcePermanentId(),
                    context.controllerId(), creatureId, opponentIds);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
        }
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId,
                                       PermanentChoiceContext.RiskyMoveOpponentChoice context) {
        if (!isPlayer(gameData, opponentId) || opponentId.equals(context.controllerId())) {
            throw new IllegalStateException("Chosen player is not an opponent");
        }

        flipAndApply(gameData, context.sourceCard().getName(), context.controllerId(),
                context.creatureId(), opponentId);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void continueAfterCreatureChoice(GameData gameData, Card sourceCard,
                                             UUID sourcePermanentId, UUID controllerId, UUID creatureId,
                                             List<UUID> opponentIds) {
        if (opponentIds.size() == 1) {
            flipAndApply(gameData, sourceCard.getName(), controllerId, creatureId, opponentIds.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.RiskyMoveOpponentChoice(
                sourceCard, sourcePermanentId, controllerId, creatureId));
        playerInputService.beginPlayerChoice(gameData, controllerId, opponentIds,
                sourceCard.getName() + " — choose an opponent.");
    }

    private void flipAndApply(GameData gameData, String sourceName, UUID controllerId,
                              UUID creatureId, UUID opponentId) {
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
        String outcome = result.heads() ? "wins" : "loses";
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " " + outcome + " the coin flip for "
                + sourceName + coinFlipService.replacementDetails(result) + "."));

        if (result.heads()) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
            return;
        }

        triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, controllerId);
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature != null) {
            creatureControlService.applyControlEffect(gameData, opponentId, creature,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                    EffectDuration.PERMANENT, null, sourceName);
        }
    }

    private List<UUID> controlledCreatureIds(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private List<UUID> opponentIds(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }

    private boolean isPlayer(GameData gameData, UUID playerId) {
        return playerId != null && gameData.playerIds.contains(playerId);
    }
}
