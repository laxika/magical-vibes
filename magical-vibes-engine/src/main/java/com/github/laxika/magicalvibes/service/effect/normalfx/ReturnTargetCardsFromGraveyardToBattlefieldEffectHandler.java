package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardsFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolveForController(gameData, entry, effect, entry.getControllerId());
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
                    && predicateEvaluationService.matchesCardPredicate(card, e.filter(), entry.getCard().getId())
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
                if (e.enterTapped()) {
                    permanent.tap();
                }
                permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, graveyardOwnerId, permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                returnedCards.add(card);
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, graveyardOwnerId, permanent, card);
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
}
