package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOtherCreaturesSharingColorWithEnteringCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Spreading Plague's trigger using the entering creature's current colors, or its
 * last-known card colors if it has already left the battlefield.
 */
@Component
@RequiredArgsConstructor
public class DestroyOtherCreaturesSharingColorWithEnteringCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyOtherCreaturesSharingColorWithEnteringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID enteringPermanentId = entry.getTriggeringPermanentId();
        Set<CardColor> enteringColors = resolveEnteringColors(gameData, entry, enteringPermanentId);
        if (enteringColors.isEmpty()) {
            return;
        }

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (enteringPermanentId != null && enteringPermanentId.equals(permanent.getId())) {
                return;
            }
            if (!gameQueryService.isCreature(gameData, permanent)) {
                return;
            }
            if (gameQueryService.getEffectiveColors(gameData, permanent).stream()
                    .anyMatch(enteringColors::contains)) {
                toDestroy.add(permanent);
            }
        });

        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), true);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private Set<CardColor> resolveEnteringColors(GameData gameData, StackEntry entry,
                                                  UUID enteringPermanentId) {
        Permanent enteringPermanent = gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (enteringPermanent != null) {
            return gameQueryService.getEffectiveColors(gameData, enteringPermanent);
        }

        Card enteringCard = findCardById(gameData, entry.getTriggeringCardId());
        if (enteringCard == null || enteringCard.getColors() == null || enteringCard.getColors().isEmpty()) {
            return Set.of();
        }
        return EnumSet.copyOf(enteringCard.getColors());
    }

    private Card findCardById(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        for (List<Card> cards : gameData.playerHands.values()) {
            for (Card card : cards) {
                if (cardId.equals(card.getId())) {
                    return card;
                }
            }
        }
        for (List<Card> cards : gameData.playerDecks.values()) {
            for (Card card : cards) {
                if (cardId.equals(card.getId())) {
                    return card;
                }
            }
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (graveyardCard != null) {
            return graveyardCard;
        }
        Card exiledCard = gameQueryService.findCardInExileById(gameData, cardId);
        if (exiledCard != null) {
            return exiledCard;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (cardId.equals(stackEntry.getCard().getId())) {
                return stackEntry.getCard();
            }
        }
        return null;
    }
}
