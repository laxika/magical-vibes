package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToOnePermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Begins the resolution-time "up to one permanent" choice. The entry's chosen permanent is cleared
 * first so a skipped or empty choice never leaves an earlier choice for the following effects; the
 * answer is recorded by {@code MultiPermanentChoiceHandlerService}.
 */
@Component
@RequiredArgsConstructor
public class ChooseUpToOnePermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseUpToOnePermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseUpToOnePermanentEffect choose = (ChooseUpToOnePermanentEffect) effect;
        entry.setChosenPermanentId(null);

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        List<UUID> candidateIds = new ArrayList<>();
        gameData.forEachPermanent((ignoredControllerId, permanent) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, choose.filter(), filterContext)) {
                candidateIds.add(permanent.getId());
            }
        });

        if (candidateIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    entry.getCard().getName() + ": there is nothing to choose."));
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), candidateIds, 1,
                new MultiPermanentChoiceContext.RecordChosenPermanent(), choose.prompt());
    }
}
