package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var chosenTypeEffect = (ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect) effect;
        CardSubtype chosenCreatureType = entry.getChosenCreatureType();
        List<UUID> legalTargetIds = chosenCreatureType == null
                ? List.of()
                : entry.getTargetCardIdsForEffect(effect).stream()
                .filter(cardId -> isLegalTarget(gameData, entry, chosenTypeEffect, chosenCreatureType, cardId))
                .toList();

        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry, legalTargetIds,
                (graveyard, card) -> gameData.addCardToHand(entry.getControllerId(), card),
                " returns ", " from graveyard to hand.");
    }

    private boolean isLegalTarget(GameData gameData, StackEntry entry,
                                  ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect effect,
                                  CardSubtype chosenCreatureType, UUID cardId) {
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        UUID graveyardOwnerId = card == null ? null : gameQueryService.findGraveyardOwnerById(gameData, cardId);
        return card != null
                && entry.getControllerId().equals(graveyardOwnerId)
                && predicateEvaluationService.matchesCardPredicate(
                card, effect.filter(chosenCreatureType), entry.getCard().getId(), gameData, graveyardOwnerId);
    }
}
