package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.ExileUpToOneMatchingCardFromEachGraveyardContext;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        CardPredicate filter = exileEffect.filter();
        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            matchingCards.addAll(graveyard.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, filter, null, gameData, controllerId))
                    .toList());
        }

        if (matchingCards.isEmpty()) return;

        gameData.graveyardTargetOperation.resolutionTimeExileUpToOneMatchingCardFromEachGraveyardResume =
                new ExileUpToOneMatchingCardFromEachGraveyardContext(
                        controllerId, entry.getSourcePermanentId(), filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards,
                gameData.orderedPlayerIds.size(),
                "Choose up to one creature or planeswalker card from each graveyard to exile.");
    }
}
