package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DeliverUntoEvilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliverUntoEvilEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentPredicate BOLAS_PLANESWALKER = new PermanentAllOfPredicate(List.of(
            new PermanentIsPlaneswalkerPredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.BOLAS)));

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DeliverUntoEvilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> legalCards = entry.getTargetCardIds().stream()
                .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                .filter(card -> card != null
                        && controllerId.equals(gameQueryService.findGraveyardOwnerById(gameData, card.getId())))
                .toList();
        if (legalCards.isEmpty()) {
            return;
        }

        if (controlsBolasPlaneswalker(gameData, controllerId)) {
            graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry,
                    legalCards.stream().map(Card::getId).toList(),
                    (graveyard, card) -> gameData.addCardToHand(controllerId, card),
                    " returns ", " from graveyard to hand.");
            return;
        }

        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId,
                List.of(), legalCards, java.util.Map.of(), List.of(), List.of(),
                CardPileDisposition.DELIVER_UNTO_EVIL));
        playerInputService.beginMultiGraveyardChoice(gameData, opponentId, legalCards,
                Math.min(2, legalCards.size()), Math.min(2, legalCards.size()),
                "Choose cards to leave in the graveyard. The rest return to the caster's hand.");
    }

    private boolean controlsBolasPlaneswalker(GameData gameData, UUID controllerId) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, BOLAS_PLANESWALKER));
    }
}
