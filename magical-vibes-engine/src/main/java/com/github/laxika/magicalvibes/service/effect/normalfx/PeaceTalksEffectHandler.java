package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PeaceTalksEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PeaceTalksEffect} into a floating effect on every player lasting "this turn and next turn"
 * ({@link Duration#THIS_TURN_AND_NEXT_TURN}), which {@code GameQueryService.isPeaceTalksActive} reads. A second one
 * is a second effect with its own duration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PeaceTalksEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PeaceTalksEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, null,
                Duration.THIS_TURN_AND_NEXT_TURN));
        gameLogService.append(gameData, GameLog.text(
                "This turn and next turn, creatures can't attack, and players and permanents "
                        + "can't be the targets of spells or activated abilities."));
        log.info("Game {} - Peace Talks active this turn and next turn", gameData.id);
    }
}
