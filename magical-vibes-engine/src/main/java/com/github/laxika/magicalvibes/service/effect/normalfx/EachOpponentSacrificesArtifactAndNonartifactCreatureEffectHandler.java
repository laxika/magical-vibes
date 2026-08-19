package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesArtifactAndNonartifactCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Perilous Predicament's simultaneous opponent choices. */
@Component
@RequiredArgsConstructor
public class EachOpponentSacrificesArtifactAndNonartifactCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentSacrificesArtifactAndNonartifactCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, apnapOpponents(gameData, entry.getControllerId()), 0, List.of(),
                entry.getControllerId(), entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachOpponentSacrificesArtifactAndNonartifactCreature context) {
        List<UUID> accumulatedIds = new ArrayList<>(context.accumulatedSacrificeIds());
        accumulatedIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex() + 1, accumulatedIds,
                context.sourceControllerId(), context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex,
                      List<UUID> accumulatedIds, UUID sourceControllerId, String sourceName) {
        List<UUID> allAccumulatedIds = new ArrayList<>(accumulatedIds);
        int currentPlayerIndex = playerIndex;

        while (currentPlayerIndex < playerIds.size()) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            currentPlayerIndex++;

            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
                continue;
            }

            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || battlefield.isEmpty()) {
                continue;
            }

            List<UUID> artifactCreatureIds = battlefield.stream()
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent)
                            && gameQueryService.isArtifact(gameData, permanent))
                    .map(Permanent::getId)
                    .toList();
            List<UUID> nonartifactCreatureIds = battlefield.stream()
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent)
                            && !gameQueryService.isArtifact(gameData, permanent))
                    .map(Permanent::getId)
                    .toList();

            List<UUID> candidateIds = new ArrayList<>(artifactCreatureIds);
            candidateIds.addAll(nonartifactCreatureIds);
            if (candidateIds.isEmpty()) {
                continue;
            }

            int requiredCount = !artifactCreatureIds.isEmpty() && !nonartifactCreatureIds.isEmpty() ? 2 : 1;
            if (candidateIds.size() <= requiredCount) {
                allAccumulatedIds.addAll(candidateIds);
                continue;
            }

            playerInputService.beginMultiPermanentChoice(gameData, playerId, candidateIds, requiredCount,
                    new MultiPermanentChoiceContext.EachOpponentSacrificesArtifactAndNonartifactCreature(
                            playerIds, currentPlayerIndex - 1, allAccumulatedIds, artifactCreatureIds,
                            nonartifactCreatureIds, requiredCount, sourceControllerId, sourceName),
                    "Choose " + requiredCount + " creature" + (requiredCount > 1 ? "s" : "")
                            + " to sacrifice.");
            return;
        }

        destructionSupport.performSimultaneousSacrifice(gameData, allAccumulatedIds);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayerIds.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(activeIndex, orderedPlayerIds.size()));
            rotated.addAll(orderedPlayerIds.subList(0, activeIndex));
            orderedPlayerIds = rotated;
        }
        return orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
