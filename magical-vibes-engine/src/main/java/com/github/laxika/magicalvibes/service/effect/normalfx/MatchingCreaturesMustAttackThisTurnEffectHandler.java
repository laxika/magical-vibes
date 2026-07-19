package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingCreaturesMustAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MatchingCreaturesMustAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MatchingCreaturesMustAttackThisTurnEffect) effect;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        final int[] count = {0};

        gameData.forEachPermanent((playerId, permanent) -> {
            // "if able" is enforced by combat: the must-attack requirement only bites creatures that
            // can legally attack, so setting the flag on ones that can't is harmless.
            if (gameQueryService.isCreature(gameData, permanent)
                    && predicateEvaluationService.matchesPermanentPredicate(permanent, e.matcher(), filterContext)) {
                permanent.setMustAttackThisTurn(true);
                count[0]++;
            }
        });

        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(entry.getCard().getName() + " forces " + count[0]
                        + " creature(s) to attack this turn if able."));
        log.info("Game {} - {} forces {} creatures to attack this turn if able",
                gameData.id, entry.getCard().getName(), count[0]);
    }
}
