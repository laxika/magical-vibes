package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Reveals a target hand and begins the choice for a card to receive persistent triggers. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (grant.filter() == null || predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), grant.filter(), sourceCardId, gameData, targetPlayerId)) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualTargetCardChoice(
                entry.getControllerId(), targetPlayerId, validIndices,
                "Choose a nonland card to receive this ability.",
                grant.slot(), grant.grantedEffects()));
    }
}
