package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayersCreaturesMustAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayersCreaturesMustAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        // Not a characteristic or control change, so it modifies the game rules and also binds creatures
        // that player gains control of or that enter later this turn (CR 611.2c).
        gameData.playerStaticEffectsUntilEndOfTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> new ArrayList<>())
                .add(new MatchingCreaturesMustAttackEffect(new PermanentControlledByPlayerPredicate(targetPlayerId)));

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " forces creatures " + playerName + " controls to attack this turn if able."));
        log.info("Game {} - {} forces {}'s creatures to attack this turn if able",
                gameData.id, entry.getCard().getName(), playerName);
    }
}
