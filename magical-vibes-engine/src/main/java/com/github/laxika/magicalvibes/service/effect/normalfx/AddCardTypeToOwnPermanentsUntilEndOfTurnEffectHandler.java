package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToOwnPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddCardTypeToOwnPermanentsUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddCardTypeToOwnPermanentsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AddCardTypeToOwnPermanentsUntilEndOfTurnEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (e.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.filter())) {
                continue;
            }
            permanent.getGrantedCardTypes().add(e.cardType());
            count++;
        }

        String typeName = e.cardType().name().charAt(0) + e.cardType().name().substring(1).toLowerCase();
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" gives " + typeName + " to " + count + " own permanent(s) until end of turn.").build());
        log.info("Game {} - {} gives {} to {} own permanent(s)", gameData.id, entry.getCard().getName(), e.cardType(), count);
    }
}
