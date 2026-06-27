package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndTransformSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapAndTransformSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapAndTransformSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        if (gameQueryService.isTransformPrevented(gameData, self)) {
            log.info("Game {} - {} can't transform (transform prevented)", gameData.id, self.getCard().getName());
            return;
        }

        // Tap
        self.tap();
        String tapLog = self.getCard().getName() + " is tapped.";
        gameBroadcastService.logAndBroadcast(gameData, tapLog);
        log.info("Game {} - {} is tapped", gameData.id, self.getCard().getName());

        // Transform
        Card originalCard = self.getOriginalCard();
        if (self.isTransformed()) {
            String backName = self.getCard().getName();
            self.setCard(originalCard);
            self.setTransformed(false);
            String logEntry = backName + " transforms into " + originalCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
        } else {
            Card backFace = originalCard.getBackFaceCard();
            if (backFace == null) {
                log.warn("Game {} - {} has no back face to transform to", gameData.id, self.getCard().getName());
                return;
            }
            String frontName = self.getCard().getName();
            self.setCard(backFace);
            self.setTransformed(true);
            String logEntry = frontName + " transforms into " + backFace.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
        }
    }
}
