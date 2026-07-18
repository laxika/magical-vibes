package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    permanent.setHasDamageToOpponentCreatureBounce(true);
                    count++;
                }
            }
        }

        String logEntry = entry.getCard().getName() + " grants damage-to-opponent creature bounce to " + count + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" grants damage-to-opponent creature bounce to " + count + " creature(s) until end of turn.").build());
        log.info("Game {} - {} grants damage bounce to {} creature(s)", gameData.id, entry.getCard().getName(), count);
    }
}
