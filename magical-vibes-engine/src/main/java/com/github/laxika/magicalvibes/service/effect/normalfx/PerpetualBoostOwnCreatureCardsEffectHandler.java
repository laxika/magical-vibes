package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetualBoostOwnCreatureCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetualBoostOwnCreatureCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetualBoostOwnCreatureCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetualBoostOwnCreatureCardsEffect boost = (PerpetualBoostOwnCreatureCardsEffect) effect;
        UUID ownerId = entry.getControllerId();
        Set<UUID> affectedCardIds = new HashSet<>();

        collectZone(gameData.playerDecks.get(ownerId), ownerId, boost, gameData, affectedCardIds);
        collectZone(gameData.playerHands.get(ownerId), ownerId, boost, gameData, affectedCardIds);
        collectZone(gameData.playerGraveyards.get(ownerId), ownerId, boost, gameData, affectedCardIds);
        collectZone(gameData.getPlayerExiledCards(ownerId), ownerId, boost, gameData, affectedCardIds);
        collectZone(gameData.playerCommandZones.get(ownerId), ownerId, boost, gameData, affectedCardIds);
        collectZone(gameData.playerCommanders.get(ownerId), ownerId, boost, gameData, affectedCardIds);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                Card card = permanent.getCard();
                boolean ownedByController = ownerId.equals(card.getOwnerId())
                        || (card.getOwnerId() == null && ownerId.equals(playerId));
                if (ownedByController && matches(gameData, card, ownerId, boost)) {
                    affectedCardIds.add(card.getId());
                }
            }
        }

        for (StackEntry stackEntry : gameData.stack) {
            Card card = stackEntry.getCard();
            if (card == null) continue;
            boolean ownedByController = ownerId.equals(card.getOwnerId())
                    || (card.getOwnerId() == null && ownerId.equals(stackEntry.getControllerId()));
            if (ownedByController && matches(gameData, card, ownerId, boost)) {
                affectedCardIds.add(card.getId());
            }
        }

        for (UUID cardId : affectedCardIds) {
            gameData.perpetualCardPowerModifiers.merge(cardId, boost.powerBoost(), Integer::sum);
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                UUID cardId = permanent.getCard().getId();
                if (affectedCardIds.contains(cardId)) {
                    permanent.setPersistentPowerModifier(
                            gameData.perpetualCardPowerModifiers.getOrDefault(cardId, 0));
                }
            }
        }

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("perpetually gives +" + boost.powerBoost() + "/+0 to "
                        + affectedCardIds.size() + " creature card(s).").build());
    }

    private void collectZone(List<Card> cards, UUID ownerId,
                             PerpetualBoostOwnCreatureCardsEffect boost, GameData gameData,
                             Set<UUID> affectedCardIds) {
        if (cards == null) return;
        for (Card card : cards) {
            if (matches(gameData, card, ownerId, boost)) {
                affectedCardIds.add(card.getId());
            }
        }
    }

    private boolean matches(GameData gameData, Card card, UUID ownerId,
                            PerpetualBoostOwnCreatureCardsEffect boost) {
        return card != null
                && card.getManaValue() <= boost.maxManaValue()
                && gameQueryService.cardHasType(card, CardType.CREATURE, gameData, ownerId);
    }
}
