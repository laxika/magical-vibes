package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureToDestroyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves The Horus Heresy's controller-first creature choices. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesCreatureToDestroyEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesCreatureToDestroyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextChoice(gameData, entry.getControllerId(), entry.getCard(),
                orderStartingWith(gameData, entry.getControllerId()), List.of());
    }

    public void beginNextChoice(GameData gameData, UUID controllerId, Card sourceCard,
                                List<UUID> remainingChooserIds, List<UUID> chosenPermanentIds) {
        List<UUID> remaining = new ArrayList<>(remainingChooserIds);
        List<UUID> chosen = new ArrayList<>(chosenPermanentIds);

        while (!remaining.isEmpty()) {
            UUID choosingPlayerId = remaining.removeFirst();
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, choosingPlayerId, p -> true);
            if (creatureIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(choosingPlayerId) + " has no creatures to choose ("
                                + sourceCard.getName() + ")."));
                continue;
            }

            if (creatureIds.size() == 1) {
                chosen.add(creatureIds.getFirst());
                continue;
            }

            PermanentChoiceContext.EachPlayerChoosesCreatureToDestroy context =
                    new PermanentChoiceContext.EachPlayerChoosesCreatureToDestroy(
                            controllerId, sourceCard, choosingPlayerId,
                            List.copyOf(remaining), List.copyOf(chosen));
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, choosingPlayerId, creatureIds, context,
                    sourceCard.getName() + " — choose a creature you control to destroy.");
            return;
        }

        destroyChosen(gameData, chosen, sourceCard.getName());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.EachPlayerChoosesCreatureToDestroy context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null || !context.choosingPlayerId().equals(
                gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Chosen permanent is no longer a creature controlled by the choosing player");
        }

        List<UUID> chosenPermanentIds = new ArrayList<>(context.chosenPermanentIds());
        chosenPermanentIds.add(permanentId);
        beginNextChoice(gameData, context.controllerId(), context.sourceCard(),
                context.remainingChooserIds(), chosenPermanentIds);
    }

    private void destroyChosen(GameData gameData, List<UUID> chosenPermanentIds, String sourceName) {
        List<Permanent> chosen = chosenPermanentIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null)
                .toList();
        if (!chosen.isEmpty()) {
            destructionSupport.destroyBatch(gameData, chosen, sourceName, false);
        }
    }

    private List<UUID> orderStartingWith(GameData gameData, UUID firstPlayerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int firstIndex = ordered.indexOf(firstPlayerId);
        if (firstIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(firstIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, firstIndex));
        return rotated;
    }
}
