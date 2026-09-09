package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect) effect;
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

        if (!validCardIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice(
                            controllerId, validCardIds, e.lifeGain(), entry.getCard(), entry.getEntryType()));
        }
    }
}
