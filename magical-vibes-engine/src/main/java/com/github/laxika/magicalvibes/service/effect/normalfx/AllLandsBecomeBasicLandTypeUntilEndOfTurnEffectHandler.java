package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllLandsBecomeBasicLandTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AllLandsBecomeBasicLandTypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllLandsBecomeBasicLandTypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AllLandsBecomeBasicLandTypeUntilEndOfTurnEffect) effect;
        int count = 0;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isLand(gameData, permanent)) {
                    continue;
                }
                GrantBasicLandTypeToTargetEffectHandler.applyBasicLandType(
                        permanent, e.subtype(), EffectDuration.UNTIL_END_OF_TURN, true);
                count++;
            }
        }
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" makes " + count + " land(s) " + e.subtype().getDisplayName()
                        + " until end of turn.").build());
    }
}
