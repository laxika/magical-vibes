package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostSelfPerBlockingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfPerBlockingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostSelfPerBlockingCreatureEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (self == null) {
            return;
        }

        List<Permanent> selfBattlefield = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(self)) {
                selfBattlefield = battlefield;
                break;
            }
        }
        if (selfBattlefield == null) return;

        int selfIndex = selfBattlefield.indexOf(self);
        if (selfIndex < 0) {
            return;
        }

        final int[] blockerCount = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargets().contains(selfIndex)) {
                blockerCount[0]++;
            }
        });

        int powerBoost = blockerCount[0] * boost.powerPerBlockingCreature();
        int toughnessBoost = blockerCount[0] * boost.toughnessPerBlockingCreature();
        self.setPowerModifier(self.getPowerModifier() + powerBoost);
        self.setToughnessModifier(self.getToughnessModifier() + toughnessBoost);

        String logEntry = self.getCard().getName() + " gets +" + powerBoost + "/+" + toughnessBoost
                + " until end of turn (" + blockerCount[0] + " blocker(s)).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{} from {} blocker(s)",
                gameData.id, self.getCard().getName(), powerBoost, toughnessBoost, blockerCount[0]);
    }
}
