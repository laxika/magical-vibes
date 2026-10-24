package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect} (Goblin Crash Pilot): sacrifices the
 * tracked permanent and, when a creature was sacrificed this way, queues the reflexive "it deals damage equal to its
 * power" ability with that creature's last-known information (CR 608.2h) as the damage source.
 *
 * <p>The sacrifice is departed at once rather than ending the step: the reflexive ability needs the creature as it
 * was on the battlefield, which a paused step can't carry to its continuation. Inside the handler's rerun window
 * ({@link AsksDeathReplacementsFirst}) a CR 616.1 question stops and resolves it again before anything changed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final DepartureService departureService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

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

        Permanent sourceSnapshot = new Permanent(target);
        DepartureService.Outcome outcome = departureService.departAsStep(gameData, entry, null,
                DepartureBatch.of(List.of(DepartureIntent.sacrifice(target.getId()))));
        if (!(outcome instanceof DepartureService.Outcome.Done done)) {
            return;
        }
        DepartureResult result = done.results().getFirst();
        // "When you sacrifice a creature this way": only a sacrificed creature has a last-known power.
        if (!result.sacrificed() || result.lastKnownPower() == null) {
            return;
        }

        StackEntry reflexiveContext = new StackEntry(
                entry.getEntryType(), entry.getCard(), entry.getControllerId(), entry.getDescription(),
                new ArrayList<>(List.of()), null, target.getId());
        reflexiveContext.setSourcePermanentSnapshot(sourceSnapshot);
        reflexiveContext.setEventValue(Math.max(0, result.lastKnownPower()));
        queueReflexiveAbilityEffectHandler.resolve(gameData, reflexiveContext,
                new QueueReflexiveAbilityEffect(
                        DealDamageToAnyTargetEffect.fromEnteringPermanent(new EventValue()), false, true));
        log.info("Game {} - {} sacrificed a creature and queued power damage", gameData.id,
                target.getCard().getName());
    }
}
