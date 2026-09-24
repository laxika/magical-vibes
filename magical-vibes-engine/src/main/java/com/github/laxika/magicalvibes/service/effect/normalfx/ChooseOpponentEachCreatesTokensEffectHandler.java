package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentEachCreatesTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Sylvan Offering's independent resolution-time opponent choices. */
@Component
@RequiredArgsConstructor
public class ChooseOpponentEachCreatesTokensEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentEachCreatesTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (ChooseOpponentEachCreatesTokensEffect) effect;
        List<UUID> opponents = opponentsOf(gameData, entry.getControllerId());
        if (opponents.isEmpty()) {
            return;
        }

        var context = new PermanentChoiceContext.ChooseOpponentEachCreatesTokens(
                entry.getControllerId(), choiceEffect.token(), entry.getCard().getName());
        if (opponents.size() == 1) {
            continueResolution(gameData, entry, choiceEffect.token(), opponents.getFirst(),
                    entry.getEffectsToResolve().indexOf(effect) + 1);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeChoice(GameData gameData, UUID chosenOpponentId,
                               PermanentChoiceContext.ChooseOpponentEachCreatesTokens context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null || !opponentsOf(gameData, context.controllerId()).contains(chosenOpponentId)) {
            throw new IllegalStateException("Invalid opponent choice");
        }

        continueResolution(gameData, entry, context.token(), chosenOpponentId,
                gameData.pendingEffectResolutionIndex);
    }

    private void continueResolution(GameData gameData, StackEntry entry,
                                    com.github.laxika.magicalvibes.model.effect.CreateTokenEffect token,
                                    UUID chosenOpponentId, int insertionIndex) {
        entry.setTargetId(chosenOpponentId);
        entry.insertEffectsToResolve(insertionIndex, List.of(
                token,
                new CreateTokenForTargetPlayerEffect(token)
        ));
    }

    private List<UUID> opponentsOf(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
