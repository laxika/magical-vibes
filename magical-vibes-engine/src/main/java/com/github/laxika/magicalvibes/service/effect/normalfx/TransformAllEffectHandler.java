package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformAllEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformAllEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformAllEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TransformAllEffect) effect;
        gameData.forEachPermanent((playerId, perm) -> {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.filter())) {
                return;
            }
            if (gameQueryService.isTransformPrevented(gameData, perm)) {
                log.info("Game {} - {} can't transform (transform prevented)", gameData.id, perm.getCard().getName());
                return;
            }
            Card originalCard = perm.getOriginalCard();
            if (!perm.isTransformed()) {
                Card backFace = originalCard.getBackFaceCard();
                if (backFace == null) {
                    return;
                }
                String frontName = perm.getCard().getName();
                perm.setCard(backFace);
                perm.setTransformed(true);
                String logEntry = frontName + " transforms into " + backFace.getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(frontName + " transforms into " , backFace, "."));
                log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
            } else {
                String backName = perm.getCard().getName();
                perm.setCard(originalCard);
                perm.setTransformed(false);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(backName + " transforms into " , originalCard, "."));
                log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
            }
        });
    }
}
