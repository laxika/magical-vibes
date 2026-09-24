package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveAllProtectionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllProtectionUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllProtectionUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var remove = (RemoveAllProtectionUntilEndOfTurnEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        if (remove.scope() == GrantScope.ALL_PERMANENTS) {
            for (UUID playerId : gameData.playerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (entry.getSourcePermanentId() != null
                            && entry.getSourcePermanentId().equals(permanent.getId())) {
                        continue;
                    }
                    if (remove.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                            permanent, remove.filter(), filterContext)) {
                        removeFrom(gameData, entry, remove, permanent);
                    }
                }
            }
            return;
        }

        UUID targetId = switch (remove.scope()) {
            case SELF -> entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId() : entry.getTargetId();
            case TARGET -> entry.getTargetId();
            default -> null;
        };
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null && (remove.filter() == null
                || predicateEvaluationService.matchesPermanentPredicate(target, remove.filter(), filterContext))) {
            removeFrom(gameData, entry, remove, target);
        }
    }

    private void removeFrom(GameData gameData, StackEntry entry,
                            RemoveAllProtectionUntilEndOfTurnEffect remove, Permanent target) {
        target.setProtectionRemovedUntilEndOfTurn(true);
        target.getProtectionFromCardTypes().clear();
        target.getProtectionFromColorsUntilEndOfTurn().clear();
        target.setProtectionFromColorlessUntilEndOfTurn(false);
        target.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn().clear();
        target.setProtectionFromOpponentCreaturesUntilEndOfTurn(false);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), remove, target.getId(), null, null,
                EffectDuration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" loses protection until end of turn.")
                .build());
        log.info("Game {} - {} loses protection until end of turn",
                gameData.id, target.getCard().getName());
    }
}
