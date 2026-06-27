package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapAllAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapAllAttackingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapAllAttackingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!p.isAttacking()) return;
            if (!gameQueryService.isCreature(gameData, p)) return;

            if (tapUntapSupport.tapPermanent(gameData, p)) {
                count[0]++;
            }
        });

        String logMsg = entry.getCard().getName() + " taps " + count[0] + " attacking creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} taps {} attacking creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }
}
