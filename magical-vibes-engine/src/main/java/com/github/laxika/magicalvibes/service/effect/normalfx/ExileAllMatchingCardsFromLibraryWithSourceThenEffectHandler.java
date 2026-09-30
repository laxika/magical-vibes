package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllMatchingCardsFromLibraryWithSourceThenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a mandatory library exile and its reflexive follow-up ability. */
@Component
@RequiredArgsConstructor
public class ExileAllMatchingCardsFromLibraryWithSourceThenEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllMatchingCardsFromLibraryWithSourceThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (ExileAllMatchingCardsFromLibraryWithSourceThenEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (sourcePermanentId == null || library == null) {
            return;
        }

        List<Card> matchingCards = library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, exileThen.exileFilter(), entry.getCard().getId()))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        library.removeAll(matchingCards);
        for (Card card : matchingCards) {
            exileService.exileCard(gameData, controllerId, card, sourcePermanentId);
            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getCard().getName() + " exiles ", card,
                            " from its controller's library."));
        }

        entry.setEventValue(matchingCards.size());
        queueReflexiveAbilityEffectHandler.resolve(gameData, entry,
                new QueueReflexiveAbilityEffect(exileThen.thenEffect()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s reflexive ability triggers."));
    }
}
