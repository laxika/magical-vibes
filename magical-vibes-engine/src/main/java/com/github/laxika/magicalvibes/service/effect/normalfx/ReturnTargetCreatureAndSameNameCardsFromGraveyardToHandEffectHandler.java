package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureAndSameNameCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureAndSameNameCardsFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureAndSameNameCardsFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        UUID targetId = entry.getTargetId();
        if (targetId == null && entry.getTargetCardIds() != null) {
            targetId = entry.getTargetCardIds().stream().findFirst().orElse(null);
        }
        if (targetId == null) {
            return;
        }

        UUID selectedTargetId = targetId;
        Card target = graveyard.stream()
                .filter(card -> card.getId().equals(selectedTargetId))
                .findFirst()
                .orElse(null);
        if (target == null || !predicateEvaluationService.matchesCardPredicate(
                target, new CardTypePredicate(CardType.CREATURE), entry.getCard().getId(),
                gameData, controllerId, null, null, entry.getXValue())) {
            return;
        }

        List<UUID> sameNameCardIds = graveyard.stream()
                .filter(card -> target.getName().equals(card.getName()))
                .map(Card::getId)
                .toList();
        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry, sameNameCardIds,
                (ignoredGraveyard, card) -> graveyardReturnSupport.addCardToHandFromGraveyard(
                        gameData, controllerId, controllerId, card),
                " returns ", " from graveyard to hand.");
    }
}
