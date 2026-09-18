package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfNextPlayerNonlandPermanentsEffect.Direction;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.OrderOfSuccessionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Order of Succession's directional creature choices and control changes. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOfSuccessionEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OrderOfSuccessionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        OrderOfSuccessionEffect order = (OrderOfSuccessionEffect) effect;
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int controllerIndex = orderedPlayerIds.indexOf(entry.getControllerId());
        if (orderedPlayerIds.size() < 2 || controllerIndex < 0) {
            return;
        }

        List<UUID> chooserIds = new ArrayList<>();
        for (int i = 0; i < orderedPlayerIds.size(); i++) {
            chooserIds.add(orderedPlayerIds.get((controllerIndex + i) % orderedPlayerIds.size()));
        }
        beginNextChoice(gameData, order.direction(), orderedPlayerIds, chooserIds, List.of(), entry.getCard().getName());
    }

    /** Prompts the next chooser, or applies all choices once the sequence is complete. */
    public void beginNextChoice(GameData gameData, Direction direction, List<UUID> orderedPlayerIds,
                                List<UUID> remainingChooserIds,
                                List<PermanentChoiceContext.OrderOfSuccessionCreatureChoice> accumulatedChoices,
                                String sourceCardName) {
        List<UUID> remaining = new ArrayList<>(remainingChooserIds);
        List<PermanentChoiceContext.OrderOfSuccessionCreatureChoice> accumulated =
                new ArrayList<>(accumulatedChoices);

        while (!remaining.isEmpty()) {
            UUID choosingPlayerId = remaining.removeFirst();
            UUID chosenFromPlayerId = nextPlayer(orderedPlayerIds, choosingPlayerId, direction);
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, chosenFromPlayerId, p -> true);
            if (creatureIds.isEmpty()) {
                String playerName = gameData.playerIdToName.get(chosenFromPlayerId);
                gameLogService.append(gameData, GameLog.text(
                        playerName + " has no creatures to choose (" + sourceCardName + ")."));
                continue;
            }

            if (creatureIds.size() == 1) {
                UUID creatureId = creatureIds.getFirst();
                accumulated.add(new PermanentChoiceContext.OrderOfSuccessionCreatureChoice(
                        creatureId, choosingPlayerId));
                logChoice(gameData, choosingPlayerId, creatureId, sourceCardName);
                continue;
            }

            PermanentChoiceContext.OrderOfSuccession context = new PermanentChoiceContext.OrderOfSuccession(
                    choosingPlayerId, chosenFromPlayerId, sourceCardName, orderedPlayerIds, direction,
                    List.copyOf(remaining), List.copyOf(accumulated));
            gameData.interaction.setPermanentChoiceContext(context);
            String chosenFromPlayerName = gameData.playerIdToName.get(chosenFromPlayerId);
            playerInputService.beginPermanentChoice(gameData, choosingPlayerId, creatureIds, context,
                    sourceCardName + " — choose a creature controlled by " + chosenFromPlayerName + ".");
            return;
        }

        applyControlChanges(gameData, accumulated, sourceCardName);
    }

    /** Records one choice, then prompts the next player or applies the completed sequence. */
    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.OrderOfSuccession context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        UUID currentControllerId = chosen == null
                ? null : gameQueryService.findPermanentController(gameData, permanentId);
        if (chosen == null || !gameQueryService.isCreature(gameData, chosen)
                || !context.chosenFromPlayerId().equals(currentControllerId)) {
            throw new IllegalStateException("Chosen creature is no longer controlled by the required player");
        }

        List<PermanentChoiceContext.OrderOfSuccessionCreatureChoice> accumulated =
                new ArrayList<>(context.accumulatedChoices());
        accumulated.add(new PermanentChoiceContext.OrderOfSuccessionCreatureChoice(
                permanentId, context.choosingPlayerId()));
        logChoice(gameData, context.choosingPlayerId(), permanentId, context.sourceCardName());

        beginNextChoice(gameData, context.direction(), context.orderedPlayerIds(),
                context.remainingChooserIds(), accumulated, context.sourceCardName());
    }

    private UUID nextPlayer(List<UUID> orderedPlayerIds, UUID choosingPlayerId, Direction direction) {
        int chooserIndex = orderedPlayerIds.indexOf(choosingPlayerId);
        int offset = direction == Direction.RIGHT ? 1 : -1;
        return orderedPlayerIds.get(Math.floorMod(chooserIndex + offset, orderedPlayerIds.size()));
    }

    private void applyControlChanges(GameData gameData,
                                     List<PermanentChoiceContext.OrderOfSuccessionCreatureChoice> choices,
                                     String sourceCardName) {
        List<ControlChange> changes = new ArrayList<>();
        for (PermanentChoiceContext.OrderOfSuccessionCreatureChoice choice : choices) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, choice.permanentId());
            if (permanent != null) {
                changes.add(new ControlChange(permanent, choice.gainingPlayerId()));
            }
        }

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (ControlChange change : changes) {
            creatureControlService.applyControlEffect(gameData, change.gainingPlayerId(), change.permanent(),
                    controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, sourceCardName);
        }
    }

    private void logChoice(GameData gameData, UUID choosingPlayerId, UUID permanentId, String sourceCardName) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null) {
            return;
        }
        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " chooses ", chosen.getCard(), " (" + sourceCardName + ")."));
        log.info("Game {} - {} chooses {} for {}", gameData.id, playerName,
                chosen.getCard().getName(), sourceCardName);
    }

    private record ControlChange(Permanent permanent, UUID gainingPlayerId) {
    }
}
