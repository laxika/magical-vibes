package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.PlayerCantCastSpellsAndAttackWithCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentChoosesPlayerForRestrictionEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetOpponentChoosesPlayerForRestrictionEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetOpponentChoosesPlayerForRestrictionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetOpponentId = entry.getTargetId();
        if (controllerId == null || targetOpponentId == null
                || !gameData.playerIds.contains(targetOpponentId)) {
            return;
        }

        String sourceCardName = entry.getCard() == null ? "Choose Your Champion" : entry.getCard().getName();
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetOpponentChoosesPlayerForRestriction(
                        controllerId, targetOpponentId, sourceCardName));
        playerInputService.beginPlayerChoice(gameData, targetOpponentId,
                gameData.orderedPlayerIds, sourceCardName + " — Choose a player.");
    }

    public void completeChoice(GameData gameData, UUID chosenPlayerId,
                               PermanentChoiceContext.TargetOpponentChoosesPlayerForRestriction context) {
        if (!gameData.playerIds.contains(chosenPlayerId)
                || !gameData.playerIds.contains(context.controllerId())) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(context.controllerId()) || playerId.equals(chosenPlayerId)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), context.sourceCardName(), null, context.controllerId(),
                    new PlayerCantCastSpellsAndAttackWithCreaturesEffect(), null, playerId, null,
                    EffectDuration.UNTIL_YOUR_NEXT_TURN, 0L));
        }

        gameLogService.append(gameData, GameLog.text(
                "Until your next turn, only you and "
                        + gameData.playerIdToName.get(chosenPlayerId)
                        + " can cast spells and attack with creatures."));
    }
}
