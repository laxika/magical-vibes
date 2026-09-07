package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect) effect;
        UUID targetId = entry.getTargetId();
        List<UUID> validCardIds = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (targetId.equals(exiled.ownerId()) && !exiled.faceDown()
                        && predicateEvaluationService.matchesCardPredicate(exiled.card(), e.filter(), null)) {
                    validCardIds.add(exiled.card().getId());
                }
            }
        }

        if (!validCardIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.FaceUpExiledCardChoice(
                            entry.getControllerId(), targetId, validCardIds));
        }
    }
}
