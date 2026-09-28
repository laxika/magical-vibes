package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureCardFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Rejoin the Fight's sequential opponent choices. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesCreatureCardFromControllerGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesCreatureCardFromControllerGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.pendingGraveyardReturnBatch = new PendingGraveyardReturnBatch(
                controllerId, List.of(), java.util.Map.of());

        List<UUID> opponents = opponentsStartingAfterController(gameData, controllerId);
        CardTypePredicate creatureFilter = new CardTypePredicate(CardType.CREATURE);
        for (UUID opponentId : opponents) {
            gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                    controllerId, 1, creatureFilter, GraveyardChoiceDestination.BATTLEFIELD,
                    false, true, false, false, false, java.util.Set.of(), java.util.Set.of(), opponentId));
        }
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }

    private List<UUID> opponentsStartingAfterController(GameData gameData, UUID controllerId) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int controllerIndex = orderedPlayers.indexOf(controllerId);
        if (controllerIndex < 0 || orderedPlayers.isEmpty()) {
            return List.of();
        }

        List<UUID> opponents = new ArrayList<>();
        for (int offset = 1; offset <= orderedPlayers.size(); offset++) {
            UUID playerId = orderedPlayers.get((controllerIndex + offset) % orderedPlayers.size());
            if (!playerId.equals(controllerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
