package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAnyNumberOfOwnedLandCardsFromExileOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutAnyNumberOfOwnedLandCardsFromExileOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAnyNumberOfOwnedLandCardsFromExileOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID ownerId = entry.getTargetId();
        if (ownerId == null) {
            return;
        }

        List<UUID> validCardIds;
        synchronized (gameData.exiledCards) {
            validCardIds = gameData.exiledCards.stream()
                    .filter(exiled -> ownerId.equals(exiled.ownerId()))
                    .filter(exiled -> !exiled.faceDown())
                    .filter(exiled -> exiled.card().hasType(CardType.LAND))
                    .map(ExiledCardEntry::card)
                    .map(card -> card.getId())
                    .toList();
        }
        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.OblivionSowerLandChoice(
                        entry.getControllerId(), ownerId, validCardIds, entry.getCard().getName()));
    }
}
