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
            List<ExiledCardEntry> faceDownOffered = new ArrayList<>();
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (!gameData.playerIds.contains(exiled.ownerId()) || controllerId.equals(exiled.ownerId())) {
                    continue;
                }
                if (!exiled.faceDown()) {
                    validCardIds.add(exiled.card().getId());
                } else if (faceDownOffered.stream()
                        .filter(offered -> PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffectHandler
                                .sameFaceDownPile(offered, exiled))
                        .count() < 2) {
                    // A face-down pile is offered as up to two indistinguishable picks; the cards that move
                    // are chosen at random from the pile when the answer comes.
                    faceDownOffered.add(exiled);
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
