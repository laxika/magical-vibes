package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnPermanentsOnCombatDamageToPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnPermanentsOnCombatDamageToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnPermanentsOnCombatDamageToPlayerEffect) effect;

        UUID defenderId = entry.getTargetId();
        int damageDealt = entry.getXValue();
        UUID attackerId = entry.getControllerId();
        String creatureName = entry.getCard().getName();

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<UUID> validIds = new ArrayList<>();
        if (defenderBattlefield != null) {
            for (Permanent perm : defenderBattlefield) {
                if (e.filter() == null || predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.filter())) {
                    validIds.add(perm.getId());
                }
            }
        }

        String targetLabel = e.filter() != null ? "creature" : "permanent";
        String targetsLabel = e.filter() != null ? "creatures" : "permanents";

        if (validIds.isEmpty()) {
            String logEntry = creatureName + "'s ability triggers, but " + gameData.playerIdToName.get(defenderId) + " has no " + targetsLabel + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        String logEntry = creatureName + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may return up to " + damageDealt + " " + (damageDealt > 1 ? targetsLabel : targetLabel) + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} combat damage trigger: {} damage, {} valid targets", gameData.id, creatureName, damageDealt, validIds.size());

        int maxCount = Math.min(damageDealt, validIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, attackerId, validIds, maxCount,
                new MultiPermanentChoiceContext.CombatDamageBounce(defenderId),
                "Return up to " + damageDealt + " " + (damageDealt > 1 ? targetsLabel : targetLabel) + " to their owner's hand.");
    
    }
}
