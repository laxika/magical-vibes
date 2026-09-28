package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeCreatureBlockableOnlyByFilterThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeCreatureBlockableOnlyByFilterThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (MakeCreatureBlockableOnlyByFilterThisTurnEffect) effect;
        // A self-targeting effect uses the source permanent even when targetId carries
        // separate context about the spell or player that caused the trigger.
        if (!grant.selfTargeting()) {
            List<UUID> targetIds = entry.targetsForEffect(effect);
            if (!targetIds.isEmpty()) {
                for (UUID targetId : targetIds) {
                    applyRestriction(gameData, entry, grant,
                            gameQueryService.findPermanentById(gameData, targetId));
                }
                return;
            }
        }

        UUID permanentId = grant.selfTargeting()
                ? (entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId())
                : entry.getTargetId();
        applyRestriction(gameData, entry, grant, gameQueryService.findPermanentById(gameData, permanentId));
    }

    private void applyRestriction(GameData gameData, StackEntry entry,
                                  MakeCreatureBlockableOnlyByFilterThisTurnEffect grant,
                                  Permanent target) {
        if (target == null) {
            return;
        }

        target.getBlockRestrictionsUntilEndOfTurn().add(
                new CanBeBlockedOnlyByFilterEffect(grant.blockerPredicate(), grant.allowedBlockersDescription()));

        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" can't be blocked this turn except by " + grant.allowedBlockersDescription() + ".").build());
        log.info("Game {} - {} can't be blocked this turn except by {}",
                gameData.id, target.getCard().getName(), grant.allowedBlockersDescription());
    }
}
