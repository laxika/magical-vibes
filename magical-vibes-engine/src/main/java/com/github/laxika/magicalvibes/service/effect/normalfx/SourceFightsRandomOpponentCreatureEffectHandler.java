package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsRandomOpponentCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SourceFightsRandomOpponentCreatureEffect}: rebuilds the pool of creatures the
 * ability controller's opponents control, picks one uniformly at random and fights the source
 * against it. An empty pool (or a source that already left the battlefield) is a no-op.
 */
@Component
@RequiredArgsConstructor
public class SourceFightsRandomOpponentCreatureEffectHandler implements NormalEffectHandlerBean {

    private final FightSupport fightSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourceFightsRandomOpponentCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId != null
                ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;
        if (source == null) return;

        List<Permanent> pool = new ArrayList<>();
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            if (battlefield.getKey().equals(entry.getControllerId())) continue;
            for (Permanent permanent : battlefield.getValue()) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    pool.add(permanent);
                }
            }
        }
        if (pool.isEmpty()) return;

        Permanent chosen = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(),
                " fights ", chosen.getCard(), " (chosen at random)."));
        fightSupport.fight(gameData, entry, source, chosen);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
