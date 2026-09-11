package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueMayPutSelectedCardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPutSelectedCardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPutSelectedCardOntoBattlefieldEffect typed = (MayPutSelectedCardOntoBattlefieldEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.isEmpty()) {
            return;
        }

        Card selectedCard = hand.getLast();
        if (!predicateEvaluationService.matchesCardPredicate(
                selectedCard, typed.predicate(), entry.getCard().getId(), gameData, entry.getControllerId())
                || selectedCard.getManaValue() > typed.manaValueAtMost()) {
            return;
        }

        String destination = typed.tapped()
                ? "Put the revealed card onto the battlefield tapped?"
                : "Put the revealed card onto the battlefield?";

        gameData.queueMayAbilityForPlayer(
                entry.getCard(),
                entry.getControllerId(),
                new MayEffect(typed, destination),
                selectedCard.getId(),
                entry.getSourcePermanentId(),
                entry.getControllerId(),
                entry.getSourcePermanentSnapshot());
        playerInputService.processNextMayAbility(gameData);
    }
}
