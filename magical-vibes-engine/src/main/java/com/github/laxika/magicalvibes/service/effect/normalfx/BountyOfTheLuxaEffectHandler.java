package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BountyOfTheLuxaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link BountyOfTheLuxaEffect}: removes all flood counters from the source enchantment;
 * if none were removed, puts a flood counter on it and draws a card, otherwise adds {C}{G}{U} to
 * the controller's pool. Because a flood counter is only ever added one at a time, this makes the
 * card alternate each turn between drawing and ramping.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BountyOfTheLuxaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BountyOfTheLuxaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;

        int removed = 0;
        if (source != null) {
            removed = source.getCounterCount(CounterType.FLOOD);
            if (removed > 0) {
                source.setCounterCount(CounterType.FLOOD, 0);
            }
        }

        if (removed == 0) {
            // No counters removed: put a flood counter on the enchantment and draw a card.
            if (source != null && !gameQueryService.cantHaveCounters(gameData, source)) {
                source.setCounterCount(CounterType.FLOOD, source.getCounterCount(CounterType.FLOOD) + 1);
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.builder().card(source.getCard()).text(" gets a flood counter.").build());
            }
            drawService.resolveDrawCard(gameData, controllerId);
        } else {
            // Counters removed: add {C}{G}{U}.
            ManaPool pool = gameData.playerManaPools.get(controllerId);
            pool.add(ManaColor.COLORLESS, 1);
            pool.add(ManaColor.GREEN, 1);
            pool.add(ManaColor.BLUE, 1);
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " adds {C}{G}{U}."));
            log.info("Game {} - {} adds C/G/U from Bounty of the Luxa", gameData.id, playerName);
        }
    }
}
