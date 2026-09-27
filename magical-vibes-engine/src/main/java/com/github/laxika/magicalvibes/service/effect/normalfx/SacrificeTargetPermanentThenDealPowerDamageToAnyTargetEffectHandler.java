package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = entry.getTargetId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, target));
        Permanent sourceSnapshot = new Permanent(target);
        if (!permanentRemovalService.sacrificePermanentToGraveyard(gameData, target)) {
            return;
        }

        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                gameData, entry.getControllerId(), target.getCard());
        gameLogService.append(gameData, GameLog.isSacrificed(target.getCard()));
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (!wasCreature) {
            return;
        }

        StackEntry reflexiveContext = new StackEntry(
                entry.getEntryType(), entry.getCard(), entry.getControllerId(), entry.getDescription(),
                new ArrayList<>(List.of()), null, target.getId());
        reflexiveContext.setSourcePermanentSnapshot(sourceSnapshot);
        reflexiveContext.setEventValue(power);
        queueReflexiveAbilityEffectHandler.resolve(gameData, reflexiveContext,
                new QueueReflexiveAbilityEffect(
                        DealDamageToAnyTargetEffect.fromEnteringPermanent(new EventValue()), false, true));
        log.info("Game {} - {} sacrificed a creature and queued power damage", gameData.id,
                target.getCard().getName());
    }
}
