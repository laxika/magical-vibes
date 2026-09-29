package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesGreatestManaValueCreatureToReturnToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Summon: Valefor's opponent-by-opponent greatest-mana-value choices. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesGreatestManaValueCreatureToReturnToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final BounceSupport bounceSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesGreatestManaValueCreatureToReturnToHandEffect.class;
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
            List<Permanent> greatestManaValueCreatures = greatestManaValueCreatures(gameData, opponentId);
            if (greatestManaValueCreatures.isEmpty()) {
                continue;
            }

            if (greatestManaValueCreatures.size() == 1) {
                chosen.add(greatestManaValueCreatures.getFirst().getId());
                continue;
            }

            PermanentChoiceContext.EachOpponentChoosesGreatestManaValueCreatureToReturnToHand context =
                    new PermanentChoiceContext.EachOpponentChoosesGreatestManaValueCreatureToReturnToHand(
                            controllerId, sourceCard, opponentId, List.copyOf(remaining), List.copyOf(chosen));
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, opponentId,
                    greatestManaValueCreatures.stream().map(Permanent::getId).toList(), context,
                    sourceCard.getName() + " — Choose a creature with the greatest mana value to return to its owner's hand.");
            return;
        }

        returnChosen(gameData, controllerId, sourceCard, chosen);
    }

    public void completeChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.EachOpponentChoosesGreatestManaValueCreatureToReturnToHand context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        List<Permanent> currentGreatest = greatestManaValueCreatures(gameData, context.opponentId());
        if (chosen == null
                || !context.opponentId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !currentGreatest.contains(chosen)) {
            throw new IllegalStateException(
                    "Chosen permanent is no longer a greatest-mana-value creature controlled by the opponent");
        }

        List<UUID> chosenPermanentIds = new ArrayList<>(context.chosenPermanentIds());
        chosenPermanentIds.add(permanentId);
        beginNextOpponent(gameData, context.controllerId(), context.sourceCard(),
                context.remainingOpponentIds(), chosenPermanentIds);
    }

    private void returnChosen(GameData gameData, UUID controllerId, Card sourceCard,
            List<UUID> chosenPermanentIds) {
        List<Permanent> chosen = new ArrayList<>();
        for (UUID permanentId : chosenPermanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                chosen.add(permanent);
            }
        }
        bounceSupport.applyReturnPermanentsToHand(gameData, new StackEntry(sourceCard, controllerId), chosen);
    }

    private List<Permanent> greatestManaValueCreatures(GameData gameData, UUID playerId) {
        List<Permanent> creatures = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();
        if (creatures.isEmpty()) {
            return List.of();
        }

        int greatestManaValue = creatures.stream()
                .mapToInt(permanent -> permanent.getCard().getManaValue())
                .max()
                .orElseThrow();
        return creatures.stream()
                .filter(permanent -> permanent.getCard().getManaValue() == greatestManaValue)
                .toList();
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex >= 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        return rotated.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}
