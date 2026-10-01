package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureToLeftThenCreatesMenaceCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Resolves Caught in a Parallel Universe's left-player creature choices and token copies. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerChoosesCreatureToLeftThenCreatesMenaceCopyEffectHandler
        implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_PROFILE =
            new CreateTokenCopyOfTargetPermanentEffect(
                    List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                    false, false, null, Set.of(Keyword.MENACE));

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesCreatureToLeftThenCreatesMenaceCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int controllerIndex = orderedPlayerIds.indexOf(entry.getControllerId());
        if (orderedPlayerIds.size() < 2 || controllerIndex < 0) {
            return;
        }

        List<UUID> chooserIds = new ArrayList<>();
        for (int i = 0; i < orderedPlayerIds.size(); i++) {
            chooserIds.add(orderedPlayerIds.get((controllerIndex + i) % orderedPlayerIds.size()));
        }
        beginNextChoice(gameData, entry.getControllerId(), entry.getCard(), orderedPlayerIds,
                chooserIds, List.of());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.CaughtInAParallelUniverseCreatureChoice context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        UUID currentControllerId = chosen == null
                ? null : gameQueryService.findPermanentController(gameData, permanentId);
        if (chosen == null || !context.chosenFromPlayerId().equals(currentControllerId)
                || !gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException(
                    "Chosen permanent is no longer a creature controlled by the player to the left");
        }

        List<PermanentChoiceContext.CaughtInAParallelUniverseCreatureCopy> accumulated =
                new ArrayList<>(context.accumulatedChoices());
        accumulated.add(new PermanentChoiceContext.CaughtInAParallelUniverseCreatureCopy(
                permanentId, context.choosingPlayerId()));
        beginNextChoice(gameData, context.controllerId(), context.sourceCard(), context.orderedPlayerIds(),
                context.remainingChooserIds(), accumulated);
    }

    private void beginNextChoice(GameData gameData, UUID controllerId, Card sourceCard,
            List<UUID> orderedPlayerIds, List<UUID> remainingChooserIds,
            List<PermanentChoiceContext.CaughtInAParallelUniverseCreatureCopy> accumulatedChoices) {
        List<UUID> remaining = new ArrayList<>(remainingChooserIds);
        List<PermanentChoiceContext.CaughtInAParallelUniverseCreatureCopy> accumulated =
                new ArrayList<>(accumulatedChoices);

        while (!remaining.isEmpty()) {
            UUID choosingPlayerId = remaining.removeFirst();
            UUID chosenFromPlayerId = playerToLeft(orderedPlayerIds, choosingPlayerId);
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(
                    gameData, chosenFromPlayerId, ignored -> true);
            if (creatureIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(chosenFromPlayerId) + " has no creatures to choose ("
                                + sourceCard.getName() + ")."));
                continue;
            }

            if (creatureIds.size() == 1) {
                accumulated.add(new PermanentChoiceContext.CaughtInAParallelUniverseCreatureCopy(
                        creatureIds.getFirst(), choosingPlayerId));
                continue;
            }

            PermanentChoiceContext.CaughtInAParallelUniverseCreatureChoice context =
                    new PermanentChoiceContext.CaughtInAParallelUniverseCreatureChoice(
                            controllerId, sourceCard, choosingPlayerId, chosenFromPlayerId,
                            orderedPlayerIds, remaining, accumulated);
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, choosingPlayerId, creatureIds, context,
                    sourceCard.getName() + " — choose a creature controlled by "
                            + gameData.playerIdToName.get(chosenFromPlayerId) + ".");
            return;
        }

        createCopies(gameData, sourceCard, accumulated);
    }

    private void createCopies(GameData gameData, Card sourceCard,
            List<PermanentChoiceContext.CaughtInAParallelUniverseCreatureCopy> choices) {
        for (PermanentChoiceContext.CaughtInAParallelUniverseCreatureCopy choice : choices) {
            StackEntry copyEntry = new StackEntry(sourceCard, choice.tokenControllerId());
            copyEntry.setTargetId(choice.permanentId());
            tokenCopyHandler.resolveForTarget(gameData, copyEntry, TOKEN_PROFILE, choice.permanentId());
        }
        log.info("Game {} - {} creates {} menace creature copies", gameData.id,
                sourceCard.getName(), choices.size());
    }

    private UUID playerToLeft(List<UUID> orderedPlayerIds, UUID playerId) {
        int playerIndex = orderedPlayerIds.indexOf(playerId);
        return orderedPlayerIds.get(Math.floorMod(playerIndex + 1, orderedPlayerIds.size()));
    }
}
