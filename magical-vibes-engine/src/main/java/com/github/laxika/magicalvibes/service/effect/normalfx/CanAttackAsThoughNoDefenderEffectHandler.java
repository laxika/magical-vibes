package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves a one-shot {@link CanAttackAsThoughNoDefenderEffect} from an activated ability by
 * installing an until-end-of-turn floating effect on the source permanent. The creature keeps
 * defender (CR-accurate "as though it didn't have defender" permission) but may attack this turn;
 * {@code GameQueryService.canAttackDespiteDefender} scans floating effects for the grant.
 * Used by Wall of Wonder. The same effect record in a STATIC slot is read directly by the layered
 * pass (e.g. Spire Serpent) and never routed here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CanAttackAsThoughNoDefenderEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CanAttackAsThoughNoDefenderEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        if (sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), sourceId, entry.getControllerId(), effect,
                sourceId, null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        String logEntry = source.getCard().getName() + " can attack this turn as though it didn't have defender.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} can attack despite defender until end of turn", gameData.id, source.getCard().getName());
    }
}
