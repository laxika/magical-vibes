package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTwoOpponentOwnedExiledCardsIntoGraveyardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutTwoOpponentOwnedExiledCardsIntoGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTwoOpponentOwnedExiledCardsIntoGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        entry.setEventValue(0);
        List<UUID> validCardIds = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (!exiled.faceDown() && gameData.playerIds.contains(exiled.ownerId())
                        && !controllerId.equals(exiled.ownerId())) {
                    validCardIds.add(exiled.card().getId());
                }
            }
        }

        if (validCardIds.size() >= 2) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice(
                            controllerId, validCardIds));
        }
    }
}
