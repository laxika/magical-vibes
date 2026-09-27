package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandomEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Chaos Defiler's controller-choice destruction for each opponent. */
@Component
@RequiredArgsConstructor
public class ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandomEffect.class;
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
            List<Permanent> nonlandPermanents = nonlandPermanents(gameData, opponentId);
            if (nonlandPermanents.isEmpty()) {
                continue;
            }

            if (nonlandPermanents.size() == 1) {
                chosen.add(nonlandPermanents.getFirst().getId());
                continue;
            }

            PermanentChoiceContext.ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandom context =
                    new PermanentChoiceContext.ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandom(
                            controllerId, sourceCard, opponentId, List.copyOf(remaining), List.copyOf(chosen));
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, controllerId,
                    nonlandPermanents.stream().map(Permanent::getId).toList(), context,
                    sourceCard.getName() + " — Choose a nonland permanent that opponent controls.");
            return;
        }

        destroyOneAtRandom(gameData, chosen, sourceCard.getName());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandom context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !context.opponentId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || gameQueryService.isLand(gameData, chosen)) {
            throw new IllegalStateException("Chosen permanent is no longer a nonland permanent controlled by the opponent");
        }

        List<UUID> chosenPermanentIds = new ArrayList<>(context.chosenPermanentIds());
        chosenPermanentIds.add(permanentId);
        beginNextOpponent(gameData, context.controllerId(), context.sourceCard(),
                context.remainingOpponentIds(), chosenPermanentIds);
    }

    private List<Permanent> nonlandPermanents(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !gameQueryService.isLand(gameData, permanent))
                .toList();
    }

    private void destroyOneAtRandom(GameData gameData, List<UUID> chosenPermanentIds, String sourceName) {
        List<Permanent> chosen = chosenPermanentIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null)
                .toList();
        if (!chosen.isEmpty()) {
            Permanent target = chosen.get(ThreadLocalRandom.current().nextInt(chosen.size()));
            destructionSupport.tryDestroyAndLog(gameData, target, sourceName, false);
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
