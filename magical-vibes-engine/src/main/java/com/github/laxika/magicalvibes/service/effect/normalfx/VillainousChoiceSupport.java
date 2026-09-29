package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Shared replacement-effect support for The Valeyard's additional villainous choices. */
@Component
@RequiredArgsConstructor
public class VillainousChoiceSupport {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void beginChoice(GameData gameData, UUID affectedPlayerId, String sourceCardName,
                            String putOption, List<String> options, String description) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (state.additionalChoicesRemaining < 0) {
            state.additionalChoicesRemaining = gameQueryService
                    .countAdditionalVillainousChoices(gameData, affectedPlayerId);
        }
        state.choicePlayerId = affectedPlayerId;
        state.choiceSourceCardName = sourceCardName;
        state.choicePutOption = putOption;
        state.choiceOptions = List.copyOf(options);
        state.choiceDescription = description;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                affectedPlayerId, null, null,
                new ChoiceContext.VillainousChoice(affectedPlayerId, sourceCardName, putOption),
                options, description));
    }

    /** Reopens the same choice when a replacement effect grants another repetition. */
    public boolean repeatIfNeeded(GameData gameData) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (state.additionalChoicesRemaining < 0) {
            return false;
        }
        if (state.additionalChoicesRemaining == 0) {
            state.additionalChoicesRemaining = -1;
            return false;
        }
        state.additionalChoicesRemaining--;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                state.choicePlayerId, null, null,
                new ChoiceContext.VillainousChoice(
                        state.choicePlayerId, state.choiceSourceCardName, state.choicePutOption),
                state.choiceOptions, state.choiceDescription));
        gameData.rerunCurrentEffectAfterInteraction = true;
        return true;
    }
}
