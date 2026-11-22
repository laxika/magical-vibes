package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomMatchingCardOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Exiles the matching card nearest the bottom of the controller's library. */
@Component
@RequiredArgsConstructor
public class ExileBottomMatchingCardOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileBottomMatchingCardOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileBottomMatchingCardOfLibraryEffect) effect;
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.getOrDefault(controllerId, List.of());
        for (int index = library.size() - 1; index >= 0; index--) {
            Card card = library.get(index);
            if (!predicateEvaluationService.matchesCardPredicate(card, exile.filter(), null)) {
                continue;
            }
            library.remove(index);
            exileService.exileCard(gameData, controllerId, card);
            entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(card.getId()), List.of(controllerId)));
            gameLogService.append(gameData, GameLog.textCardText(gameData.playerIdToName.get(controllerId) + " exiles ",
                    card, " from the bottom of their library."));
            return;
        }
    }
}
