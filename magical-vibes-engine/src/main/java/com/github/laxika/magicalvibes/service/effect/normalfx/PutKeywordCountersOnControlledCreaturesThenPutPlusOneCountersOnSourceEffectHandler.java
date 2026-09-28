package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffect kathrilEffect =
                (PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffect) effect;
        List<CounterType> matchingCounterTypes = kathrilEffect.counterTypes().stream()
                .filter(counterType -> counterType.grantedKeyword() != null)
                .filter(counterType -> hasKeywordInControllerGraveyard(gameData, entry, counterType))
                .distinct()
                .toList();
        beginCounterPlacement(gameData, entry, matchingCounterTypes, 0);
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.KathrilKeywordCounterChoice context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect is waiting for a creature choice");
        }

        int placed = 0;
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        UUID chosenController = gameQueryService.findPermanentController(gameData, chosenPermanentId);
        if (chosen != null && gameQueryService.isCreature(gameData, chosen)
                && entry.getControllerId().equals(chosenController)) {
            placed = permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, chosen, context.counterType(), 1);
        }
        beginCounterPlacement(gameData, entry, context.remainingCounterTypes(),
                context.countersPlaced() + placed);
    }

    private void beginCounterPlacement(GameData gameData, StackEntry entry,
                                       List<CounterType> remainingCounterTypes, int countersPlaced) {
        if (remainingCounterTypes.isEmpty()) {
            putPlusOneCountersOnSource(gameData, entry, countersPlaced);
            return;
        }

        CounterType counterType = remainingCounterTypes.getFirst();
        List<UUID> creatureIds = controlledCreatureIds(gameData, entry.getControllerId());
        if (creatureIds.isEmpty()) {
            beginCounterPlacement(gameData, entry, remainingCounterTypes.subList(1,
                    remainingCounterTypes.size()), countersPlaced);
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent target = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            int placed = target == null ? 0 : permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, counterType, 1);
            beginCounterPlacement(gameData, entry, remainingCounterTypes.subList(1,
                    remainingCounterTypes.size()), countersPlaced + placed);
            return;
        }

        List<CounterType> remainingAfterChoice = remainingCounterTypes.subList(1,
                remainingCounterTypes.size());
        PermanentChoiceContext.KathrilKeywordCounterChoice context =
                new PermanentChoiceContext.KathrilKeywordCounterChoice(
                        entry.getCard(), entry.getControllerId(), entry.getSourcePermanentId(),
                        counterType, remainingAfterChoice, countersPlaced);
        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), creatureIds, context,
                "Choose a creature you control to receive a "
                        + permanentCounterSupport.counterTypeName(counterType) + " counter.");
    }

    private void putPlusOneCountersOnSource(GameData gameData, StackEntry entry, int countersPlaced) {
        if (countersPlaced <= 0 || entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, source, CounterType.PLUS_ONE_PLUS_ONE, countersPlaced);
        }
    }

    private boolean hasKeywordInControllerGraveyard(GameData gameData, StackEntry entry,
                                                    CounterType counterType) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of());
        return graveyard.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .anyMatch(card -> card.getKeywords().contains(counterType.grantedKeyword()));
    }

    private List<UUID> controlledCreatureIds(GameData gameData, UUID controllerId) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }
}
