package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureThenLoseLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the exile-plus-last-known-toughness life-loss effect. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureThenLoseLifeEqualToToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final LifeSupport lifeSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureThenLoseLifeEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTargetCreatureThenLoseLifeEqualToToughnessEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int toughness = Math.max(0, gameQueryService.getEffectiveToughness(gameData, target));
        if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
        log.info("Game {} - {} is exiled by {}",
                gameData.id, target.getCard().getName(), entry.getCard().getName());
        permanentRemovalService.removeOrphanedAuras(gameData);

        UUID controllerId = entry.getControllerId();
        if (controllerId != null && !controlsExemptPermanent(gameData, controllerId, exile)) {
            lifeSupport.applyLifeLoss(gameData, controllerId, toughness, entry.getCard().getName());
        }
    }

    private boolean controlsExemptPermanent(GameData gameData, UUID playerId,
                                            ExileTargetCreatureThenLoseLifeEqualToToughnessEffect effect) {
        if (effect.exemptIfControls() == null) {
            return false;
        }
        var battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, effect.exemptIfControls()));
    }
}
