package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnTopOfLibraryAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutSelfOnTopOfLibraryAtEndOfCombatEffect} by scheduling the source permanent for
 * a delayed end-of-combat move to the top of its owner's library.
 */
@Component
@RequiredArgsConstructor
public class PutSelfOnTopOfLibraryAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSelfOnTopOfLibraryAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        gameData.queueDelayedAction(new DelayedPermanentAction(self.getId(),
                DelayedPermanentActionKind.PUT_ON_TOP_OF_LIBRARY_AT_END_OF_COMBAT));
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(),
                " will be put on top of its owner's library at end of combat."));
    }
}
