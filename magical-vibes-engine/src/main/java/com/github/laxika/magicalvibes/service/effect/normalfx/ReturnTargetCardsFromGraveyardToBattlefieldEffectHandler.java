package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardsFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnTargetCardsFromGraveyardToBattlefieldEffect) effect;
        if (returnEffect.source() == GraveyardSearchScope.ALL_GRAVEYARDS) {
            resolveFromAllGraveyards(gameData, entry, returnEffect);
        } else {
            resolveForController(gameData, entry, effect, entry.getControllerId());
        }
    }

    private void resolveFromAllGraveyards(GameData gameData, StackEntry entry,
                                          ReturnTargetCardsFromGraveyardToBattlefieldEffect effect) {
        List<GraveyardCard> cardsToReturn = new ArrayList<>();
        for (UUID targetCardId : entry.getTargetCardIds()) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
            if (graveyardOwnerId == null) {
                continue;
            }
            Card card = gameData.playerGraveyards.getOrDefault(graveyardOwnerId, List.of()).stream()
                    .filter(graveyardCard -> graveyardCard.getId().equals(targetCardId))
                    .findFirst().orElse(null);
            if (card != null && predicateEvaluationService.matchesCardPredicate(
                    card, effect.filter(), entry.getCard().getId(), gameData, graveyardOwnerId,
                    null, null, entry.getXValue())) {
                cardsToReturn.add(new GraveyardCard(graveyardOwnerId, card));
            }
        }

        if (cardsToReturn.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Card> returnedCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (GraveyardCard graveyardCard : cardsToReturn) {
                Card card = graveyardCard.card();
                if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                    continue;
                }
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                Permanent permanent = new Permanent(card);
                graveyardReturnSupport.applyPermanentGrants(permanent, effect.grantColor(), effect.grantSubtype());
                if (effect.enterTapped()) {
                    permanent.tap();
                }
                permanent.setEnteredFromGraveyardOwnerId(graveyardCard.ownerId());
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                returnedCards.add(card);
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!returnedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " returns " + returnedCards.size()
                            + " card(s) from graveyards to the battlefield."));
        }
    }

    public void resolveForController(GameData gameData, StackEntry entry, CardEffect effect,
                                     UUID graveyardOwnerId) {
        var e = (ReturnTargetCardsFromGraveyardToBattlefieldEffect) effect;
        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        if (graveyard == null || graveyard.isEmpty() || entry.getTargetCardIds().isEmpty()) {
            return;
        }

        Set<UUID> trackedIds = e.fromBattlefieldThisTurn()
                ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(graveyardOwnerId, Set.of())
                : null;
        List<Card> cardsToReturn = new ArrayList<>();
        int totalManaValue = 0;
        for (UUID targetCardId : entry.getTargetCardIds()) {
            Card card = graveyard.stream()
                    .filter(graveyardCard -> graveyardCard.getId().equals(targetCardId))
                    .findFirst().orElse(null);
            if (card != null
                    && (trackedIds == null || trackedIds.contains(card.getId()))
                    && predicateEvaluationService.matchesCardPredicate(card, e.filter(), entry.getCard().getId(),
                    gameData, graveyardOwnerId, null, null, entry.getXValue())
                    && (!e.hasTotalManaValueCap()
                    || totalManaValue + card.getManaValue() <= e.maxTotalManaValue())) {
                cardsToReturn.add(card);
                totalManaValue += card.getManaValue();
            }
        }

        if (cardsToReturn.isEmpty()) {
            return;
        }

        Set<CardType> enterTappedTypes =
                battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Card> returnedCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Card card : cardsToReturn) {
                if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                    continue;
                }
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                Permanent permanent = new Permanent(card);
                graveyardReturnSupport.applyPermanentGrants(permanent, e.grantColor(), e.grantSubtype());
                if (e.enterTapped()) {
                    permanent.tap();
                }
                permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, graveyardOwnerId, permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                returnedCards.add(card);
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, graveyardOwnerId, permanent, card);
                if (e.counterType() != null && e.counterCount() > 0) {
                    permanentCounterSupport.placeCounterOnPermanent(
                            gameData, entry, permanent, e.counterType(), e.counterCount());
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!returnedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(graveyardOwnerId) + " returns " + returnedCards.size()
                            + " card(s) from the graveyard to the battlefield."));
        }
    }

    private record GraveyardCard(UUID ownerId, Card card) {
    }
}
