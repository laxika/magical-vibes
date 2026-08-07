package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MustBeBlockedIfAbleThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustBeBlockedIfAbleThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (((MustBeBlockedIfAbleThisTurnEffect) effect).scope() == GrantScope.OWN_CREATURES) {
            resolveForOwnCreatures(gameData, entry);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setMustBeBlockedThisTurn(true);

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " must be blocked this turn if able."));
        log.info("Game {} - {} must be blocked this turn if able", gameData.id, target.getCard().getName());
    }

    private void resolveForOwnCreatures(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            permanent.setMustBeBlockedThisTurn(true);
            count++;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " makes " + count + " creature(s) must be blocked this turn if able."));
        log.info("Game {} - {} makes {} own creature(s) must be blocked this turn", gameData.id,
                entry.getCard().getName(), count);
    }
}
