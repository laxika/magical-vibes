package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureAndSameNameCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureAndSameNameCardsFromGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnCardsHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureAndSameNameCardsFromGraveyardToBattlefieldEffect.class;
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
        ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect =
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardNamedPredicate(target.getName()), Integer.MAX_VALUE, false, true);
        returnCardsHandler.resolveForController(gameData, entry, returnEffect, controllerId, sameNameCardIds);
    }
}
