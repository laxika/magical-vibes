package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeColorlessUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeColorlessUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The targeted form (Ersatz Gnomes) always affects the chosen permanent; the self-scoped form
        // (Raging Spirit) affects its own source, falling back to the target id when there is none.
        boolean targeted = effect instanceof BecomeColorlessUntilEndOfTurnEffect e && e.targeted();
        UUID selfId = targeted || entry.getSourcePermanentId() == null
                ? entry.getTargetId()
                : entry.getSourcePermanentId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        // CR 613 layer engine: "becomes colorless until end of turn" is a floating layer-5
        // color-setting effect with an empty color set and its own timestamp. The legacy transient
        // fields are seeded for direct getEffectiveColor callers.
        self.getTransientColors().clear();
        self.setColorOverridden(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(),
                new BecomeColorlessUntilEndOfTurnEffect(false),
                self.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " becomes colorless until end of turn."));
        log.info("Game {} - {} becomes colorless until end of turn", gameData.id, self.getCard().getName());
    }
}
