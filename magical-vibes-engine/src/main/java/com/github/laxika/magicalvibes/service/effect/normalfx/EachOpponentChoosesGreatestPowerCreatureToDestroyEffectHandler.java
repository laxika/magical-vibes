package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesGreatestPowerCreatureToDestroyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Highcliff Felidar's controller-choice destruction for each opponent. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesGreatestPowerCreatureToDestroyEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesGreatestPowerCreatureToDestroyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextOpponent(gameData, entry.getControllerId(), entry.getCard(),
                apnapOpponents(gameData, entry.getControllerId()), List.of());
    }

    public void beginNextOpponent(GameData gameData, UUID controllerId, Card sourceCard,
            List<UUID> remainingOpponentIds, List<UUID> chosenPermanentIds) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        List<UUID> chosen = new ArrayList<>(chosenPermanentIds);

        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<Permanent> greatestPowerCreatures = greatestPowerCreatures(gameData, opponentId);
            if (greatestPowerCreatures.isEmpty()) {
                continue;
            }

            if (greatestPowerCreatures.size() == 1) {
                chosen.add(greatestPowerCreatures.getFirst().getId());
                continue;
            }

            PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToDestroy context =
                    new PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToDestroy(
                            controllerId, sourceCard, opponentId, List.copyOf(remaining), List.copyOf(chosen));
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, controllerId,
                    greatestPowerCreatures.stream().map(Permanent::getId).toList(), context,
                    sourceCard.getName() + " — Choose a creature with the greatest power to destroy.");
            return;
        }

        destroyChosen(gameData, chosen, sourceCard.getName());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToDestroy context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        List<Permanent> currentGreatest = greatestPowerCreatures(gameData, context.opponentId());
        if (chosen == null
                || !context.opponentId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !currentGreatest.contains(chosen)) {
            throw new IllegalStateException(
                    "Chosen permanent is no longer a greatest-power creature controlled by the opponent");
        }

        List<UUID> chosenPermanentIds = new ArrayList<>(context.chosenPermanentIds());
        chosenPermanentIds.add(permanentId);
        beginNextOpponent(gameData, context.controllerId(), context.sourceCard(),
                context.remainingOpponentIds(), chosenPermanentIds);
    }

    private List<Permanent> greatestPowerCreatures(GameData gameData, UUID playerId) {
        List<Permanent> creatures = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();
        if (creatures.isEmpty()) {
            return List.of();
        }

        int greatestPower = creatures.stream()
                .mapToInt(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                .max()
                .orElseThrow();
        return creatures.stream()
                .filter(permanent -> gameQueryService.getEffectivePower(gameData, permanent) == greatestPower)
                .toList();
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

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        return rotated.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}
