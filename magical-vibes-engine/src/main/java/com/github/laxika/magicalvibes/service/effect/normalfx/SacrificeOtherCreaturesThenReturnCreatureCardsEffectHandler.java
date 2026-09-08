package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreaturesThenReturnCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Bringer of the Last Gift's cast-only mass sacrifice and return effect. */
@Component
@RequiredArgsConstructor
public class SacrificeOtherCreaturesThenReturnCreatureCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOtherCreaturesThenReturnCreatureCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, List<Card>> cardsByGraveyard = snapshotCreatureCards(gameData);

        SacrificePermanentsEffect sacrificeEffect = new SacrificePermanentsEffect(
                Integer.MAX_VALUE,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                SacrificeRecipient.EACH_PLAYER);
        sacrificePermanentsEffectHandler.resolve(gameData, entry, sacrificeEffect);

        Map<UUID, List<Card>> cardsToReturn = removeCardsStillInGraveyards(gameData, cardsByGraveyard);
        graveyardReturnSupport.putCardsOntoBattlefieldSimultaneously(
                gameData, cardsToReturn, false, null);
    }

    private Map<UUID, List<Card>> snapshotCreatureCards(GameData gameData) {
        Map<UUID, List<Card>> cardsByGraveyard = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }
            List<Card> creatureCards = graveyard.stream()
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .toList();
            if (!creatureCards.isEmpty()) {
                cardsByGraveyard.put(playerId, new ArrayList<>(creatureCards));
            }
        }
        return cardsByGraveyard;
    }

    private Map<UUID, List<Card>> removeCardsStillInGraveyards(
            GameData gameData, Map<UUID, List<Card>> cardsByGraveyard) {
        Map<UUID, List<Card>> cardsToReturn = new LinkedHashMap<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Map.Entry<UUID, List<Card>> entry : cardsByGraveyard.entrySet()) {
                List<Card> cards = new ArrayList<>();
                for (Card card : entry.getValue()) {
                    if (gameData.playerGraveyards.getOrDefault(entry.getKey(), List.of()).stream()
                            .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()))) {
                        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                        cards.add(card);
                    }
                }
                if (!cards.isEmpty()) {
                    cardsToReturn.put(entry.getKey(), cards);
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        return cardsToReturn;
    }
}
