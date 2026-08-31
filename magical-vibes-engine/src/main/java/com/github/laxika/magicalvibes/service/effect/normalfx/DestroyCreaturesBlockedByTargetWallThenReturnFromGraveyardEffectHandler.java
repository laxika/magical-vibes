package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent targetWall = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (targetWall == null) {
            return;
        }

        Map<UUID, UUID> historicalControllerByCreatureId = new HashMap<>();
        Map<UUID, UUID> controllerByCreatureId = gameData.combatBlockOpponentControllerIdsThisTurn
                .getOrDefault(targetWall.getId(), Map.of());
        List<Permanent> toDestroy = new ArrayList<>();
        for (UUID creatureId : gameData.combatOpponentIdsBlockedByThisTurn
                .getOrDefault(targetWall.getId(), Set.of())) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
                continue;
            }

            UUID historicalControllerId = controllerByCreatureId.get(creatureId);
            if (historicalControllerId == null) {
                historicalControllerId = gameQueryService.findPermanentController(gameData, creatureId);
            }
            if (historicalControllerId == null) {
                continue;
            }
            toDestroy.add(creature);
            historicalControllerByCreatureId.put(creatureId, historicalControllerId);
        }

        List<Permanent> destroyed = destructionSupport.destroyBatchCollecting(
                gameData, toDestroy, entry.getCard().getName(), true);
        if (destroyed.isEmpty()) {
            return;
        }

        gameData.pendingGraveyardReturnBatch = new PendingGraveyardReturnBatch(
                entry.getControllerId(), List.of(), Map.of(), true);
        CardPredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        for (Permanent deadCreature : destroyed) {
            UUID graveyardOwnerId = historicalControllerByCreatureId.get(deadCreature.getId());
            if (graveyardOwnerId == null) {
                continue;
            }
            gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                    graveyardOwnerId, 1, creatureCard, GraveyardChoiceDestination.BATTLEFIELD,
                    false, true, false, false, Set.of(), Set.of()));
        }
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
