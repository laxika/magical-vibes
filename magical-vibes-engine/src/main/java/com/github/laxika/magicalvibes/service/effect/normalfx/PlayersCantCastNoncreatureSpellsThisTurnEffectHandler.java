package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastNoncreatureSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayersCantCastNoncreatureSpellsThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersCantCastNoncreatureSpellsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PlayersCantCastNoncreatureSpellsThisTurnEffect restriction =
                (PlayersCantCastNoncreatureSpellsThisTurnEffect) effect;
        if (restriction.opponentsOnly()) {
            gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(entry.getControllerId()))
                    .forEach(gameData.playersCantCastNoncreatureSpellsThisTurn::add);
        } else {
            gameData.playersCantCastNoncreatureSpellsThisTurn.addAll(gameData.orderedPlayerIds);
        }
        String logEntry = restriction.opponentsOnly()
                ? gameData.playerIdToName.get(entry.getControllerId())
                + "'s opponents can't cast noncreature spells this turn."
                : "Players can't cast noncreature spells this turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
    }
}
