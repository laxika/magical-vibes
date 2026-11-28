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
        List<UUID> validCardIds = twoCardOffer(gameData, controllerId);

        if (validCardIds.size() >= 2) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice(
                            controllerId, validCardIds));
        }
    }

    /**
     * The picks offered for "put two cards your opponents own from exile into their owners' graveyards": each face-up
     * card an opponent owns, and each face-down pile (what exiled the cards and when) as up to two indistinguishable
     * picks, whose cards are chosen at random from the pile when the answer comes (2015-08-25 ruling). Shared by
     * Ulamog's Nullifier and Ulamog's Despoiler.
     */
    public static List<UUID> twoCardOffer(GameData gameData, UUID controllerId) {
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
                    faceDownOffered.add(exiled);
                    validCardIds.add(exiled.card().getId());
                }
            }
        }
        return validCardIds;
    }
}
