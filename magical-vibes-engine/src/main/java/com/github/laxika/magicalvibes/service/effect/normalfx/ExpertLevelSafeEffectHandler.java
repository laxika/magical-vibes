package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExpertLevelSafeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExpertLevelSafeEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Expert-Level Safe's private choices and its matching or nonmatching branch. */
@Component
@RequiredArgsConstructor
public class ExpertLevelSafeEffectHandler implements NormalEffectHandlerBean {

    private static final int MIN_CHOICE = 1;
    private static final int MAX_CHOICE = 3;

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExpertLevelSafeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExpertLevelSafeState state = gameData.expertLevelSafe;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.sourcePermanentId = entry.getSourcePermanentId();
            state.controllerId = entry.getControllerId();
            state.opponentId = entry.getTargetId();
            state.currentPlayerId = state.controllerId;
            prompt(gameData, entry);
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }

        if (state.currentPlayerId.equals(state.controllerId)) {
            state.controllerChoice = gameData.chosenXValue;
            state.currentPlayerId = state.opponentId;
            gameData.chosenXValue = null;
            prompt(gameData, entry);
            return;
        }

        state.opponentChoice = gameData.chosenXValue;
        gameData.chosenXValue = null;
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(state.controllerId) + " and "
                        + gameData.playerIdToName.get(state.opponentId) + " reveal their choices: "
                        + state.controllerChoice + " and " + state.opponentChoice + "."));

        List<CardEffect> followUp = state.controllerChoice.equals(state.opponentChoice)
                ? List.of(new SacrificeSelfEffect(), new PutAllCardsExiledWithSourceIntoOwnersHandsEffect())
                : List.of(new ExileTopCardsToSourceEffect(1, true));
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Could not locate Expert-Level Safe effect on stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, followUp);
        state.reset();
    }

    private void prompt(GameData gameData, StackEntry entry) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                gameData.expertLevelSafe.currentPlayerId,
                MIN_CHOICE,
                MAX_CHOICE,
                "Choose 1, 2, or 3 for " + entry.getCard().getName() + ".",
                entry.getCard().getName()));
    }
}
