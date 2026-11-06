package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
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
        // A library search's selected card is held out of every zone for this decision (the stack entry's chosen
        // card); otherwise the selected card is the one the preceding effect put into the hand.
        Card heldCard = entry.getChosenObjectCard();
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (heldCard == null && (hand == null || hand.isEmpty())) {
            return;
        }

        Card selectedCard = heldCard != null ? heldCard : hand.getLast();
        if (!predicateEvaluationService.matchesCardPredicate(
                selectedCard, typed.predicate(), entry.getCard().getId(), gameData, entry.getControllerId())
                || selectedCard.getManaValue() > typed.manaValueAtMost()) {
            return;
        }

        String destination = typed.tapped()
                ? "Put the revealed card onto the battlefield tapped?"
                : "Put the revealed card onto the battlefield?";

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                new ArrayList<>(List.of(typed)),
                entry.getCard().getName() + " - " + destination,
                selectedCard.getId(),
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                null,
                null,
                entry.getControllerId(),
                entry.getSourcePermanentSnapshot()).withHeldCard(heldCard));
        playerInputService.processNextMayAbility(gameData);
    }
}
