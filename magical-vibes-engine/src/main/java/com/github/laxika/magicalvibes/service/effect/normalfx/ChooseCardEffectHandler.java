package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService.LocatedCard;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Asks which of the named cards is chosen and resolves again with the answer, before anything changed: the answer is
 * recorded by {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen} as the one of the resolution-time
 * graveyard exile is. Records {@link StepKind#CHOOSE}.
 */
@Component
@RequiredArgsConstructor
public class ChooseCardEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ChooseCardEffect) effect;
        List<LocatedCard> candidates = subjectResolutionService.locatedCards(gameData, entry, effect, choose.cards());
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        if (state.resolutionTimeExileThenEffectChoiceMade) {
            UUID chosenId = state.resolutionTimeExileThenEffectChosenCardId;
            state.resolutionTimeExileThenEffectChoiceMade = false;
            state.resolutionTimeExileThenEffectChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            entry.recordStep(StepKind.CHOOSE, StepOutcome.NONE);
            candidates.stream()
                    .filter(located -> located.card().getId().equals(chosenId))
                    .findFirst()
                    .ifPresent(chosen -> record(gameData, entry, chosen));
            return;
        }
        if (candidates.isEmpty()) {
            entry.recordStep(StepKind.CHOOSE, StepOutcome.NONE);
            return;
        }
        if (candidates.size() == 1 && !choose.alwaysAsked()) {
            record(gameData, entry, candidates.getFirst());
            return;
        }
        // The step records as it resolves again with the answer: until then the candidates are read as they are now,
        // and may be what the CHOOSE step before this one chose.
        state.resolutionTimeExileThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(),
                candidates.stream().map(LocatedCard::card).toList(), 1, 1, "Choose " + choose.label() + ".");
    }

    private void record(GameData gameData, StackEntry entry, LocatedCard chosen) {
        UUID cardId = chosen.card().getId();
        entry.recordStep(StepKind.CHOOSE, StepOutcome.ofObjects(List.of(cardId),
                chosen.ownerId() == null ? List.of() : List.of(chosen.ownerId()),
                List.of(gameData.boundToStay(new MovedCard(cardId, cardId, chosen.zone(), chosen.ownerId())))));
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " chooses ", chosen.card(), "."));
    }
}
