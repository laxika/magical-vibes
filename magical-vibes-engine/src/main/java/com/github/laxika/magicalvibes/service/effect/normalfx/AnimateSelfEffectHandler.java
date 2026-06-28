package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnimateSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnimateSelfEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (self == null) {
            return;
        }

        int xValue = entry.getXValue();
        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(xValue);
        self.setAnimatedToughness(xValue);
        self.getTransientSubtypes().clear();
        self.getTransientSubtypes().addAll(e.grantedSubtypes());

        String logEntry = self.getCard().getName() + " becomes a " + xValue + "/" + xValue + " artifact creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature", gameData.id, self.getCard().getName(), xValue, xValue);
    }
}
