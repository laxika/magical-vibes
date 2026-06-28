package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapAllAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkipNextUntapAllAttackingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipNextUntapAllAttackingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!p.isAttacking()) return;
            if (!gameQueryService.isCreature(gameData, p)) return;

            p.setSkipUntapCount(p.getSkipUntapCount() + 1);
            count[0]++;
        });

        String logMsg = entry.getCard().getName() + " prevents " + count[0] + " attacking creature(s) from untapping during their controller's next untap step.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} skip next untap set on {} attacking creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }
}
