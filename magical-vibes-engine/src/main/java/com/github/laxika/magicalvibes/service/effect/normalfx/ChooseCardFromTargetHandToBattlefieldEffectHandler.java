package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseCardFromTargetHandToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromTargetHandToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseCardFromTargetHandToBattlefieldEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());

        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), e.predicate(), sourceCardId, gameData, targetPlayerId)) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TargetedHandBattlefieldChoice(
                entry.getControllerId(), targetPlayerId, validIndices,
                "You may put a " + e.label() + " card from their hand onto the battlefield under your control.",
                e.grantHaste(), e.sacrificeAtEndStep()));
    }
}
