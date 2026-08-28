package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeAnyNumberOfPermanentsEffect} by opening a multi-permanent choice.
 * Completion records the actual sacrifice count on the parked stack entry before resolution
 * resumes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeAnyNumberOfPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAnyNumberOfPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeAnyNumberOfPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId());
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                    eligibleIds.add(permanent.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            entry.setEventValue(0);
            if (e.recordSacrificedPower()) {
                entry.setSacrificedPower(0);
            }
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " has no matching permanents to sacrifice."));
            log.info("Game {} - {} has no matching permanents to sacrifice for {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                new MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount(
                        entry, e.recordSacrificedPower()),
                "Choose any number of permanents to sacrifice.");
    }
}
