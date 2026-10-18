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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SourceFightsRandomOpponentCreatureEffect}: the source fights the creature that
 * was chosen at random when the ability went on the stack. A missing target or a source that
 * already left the battlefield is a no-op.
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

        UUID targetId = entry.getTargetId();
        Permanent chosen = targetId != null ? gameQueryService.findPermanentById(gameData, targetId) : null;
        if (chosen == null || !gameQueryService.isCreature(gameData, chosen)) return;

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(),
                " fights ", chosen.getCard(), "."));
        fightSupport.fight(gameData, entry, source, chosen);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
