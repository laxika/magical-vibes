package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentsThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves multi-target bounce followed by a rider that uses the number or statistic of permanents returned.
 */
@Slf4j
@Component
public class ReturnTargetPermanentsThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final EffectResolutionService effectResolutionService;
    private final GameOutcomeService gameOutcomeService;

    public ReturnTargetPermanentsThenEffectHandler(
            GameQueryService gameQueryService,
            PermanentRemovalService permanentRemovalService,
            GameLogService gameLogService,
            @Lazy EffectResolutionService effectResolutionService,
            GameOutcomeService gameOutcomeService) {
        this.gameQueryService = gameQueryService;
        this.permanentRemovalService = permanentRemovalService;
        this.gameLogService = gameLogService;
        this.effectResolutionService = effectResolutionService;
        this.gameOutcomeService = gameOutcomeService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentsThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetPermanentsThenEffect) effect;

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        List<Permanent> toReturn = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                toReturn.add(target);
            }
        }

        List<Permanent> returned = new ArrayList<>();
        int eventValue = 0;
        for (Permanent target : toReturn) {
            int statValue = snapshotStat(gameData, target, e.stat());
            if (permanentRemovalService.removePermanentToHand(gameData, target)) {
                returned.add(target);
                eventValue += statValue;
                gameLogService.append(gameData,
                        GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
                log.info("Game {} - {} returned to owner's hand by {}",
                        gameData.id, target.getCard().getName(), entry.getCard().getName());
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (e.thenEffect() == null || returned.isEmpty()) {
            return;
        }

        StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(e.thenEffect()), entry.getTargetId(), entry.getSourcePermanentId());
        thenEntry.setEventValue(e.stat() == EventStat.NONE ? returned.size() : eventValue);
        thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        effectResolutionService.resolveEffects(gameData, thenEntry);
        gameOutcomeService.checkWinCondition(gameData);
    }

    private int snapshotStat(GameData gameData, Permanent target, EventStat stat) {
        return switch (stat) {
            case NONE -> 0;
            case MANA_VALUE -> target.getCard().getManaValue();
            case TOUGHNESS -> gameQueryService.getEffectiveToughness(gameData, target);
            case POWER -> gameQueryService.getPowerBasedDamage(gameData, target);
            case BASIC_LAND_SEARCH_COUNT -> 0;
        };
    }
}
