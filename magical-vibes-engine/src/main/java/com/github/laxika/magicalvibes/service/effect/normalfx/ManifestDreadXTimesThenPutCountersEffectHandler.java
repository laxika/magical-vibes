package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadXTimesThenPutCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Valgavoth's Onslaught's repeated manifest-dread and counter-placement effect. */
@Component
@RequiredArgsConstructor
public class ManifestDreadXTimesThenPutCountersEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ManifestDreadEffectHandler manifestDreadEffectHandler;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestDreadXTimesThenPutCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ManifestDreadXTimesThenPutCountersEffect onslaught =
                (ManifestDreadXTimesThenPutCountersEffect) effect;
        if (!onslaught.initialized()) {
            int count = amountEvaluationService.evaluate(
                    gameData, onslaught.amount(), AmountContext.forStackEntry(entry, null));
            onslaught = new ManifestDreadXTimesThenPutCountersEffect(
                    onslaught.amount(), count, count, List.of(), false);
            replaceCurrentEffect(entry, effect, onslaught);
        }

        if (onslaught.awaitingManifestationChoice()) {
            List<UUID> manifestedPermanentIds = new ArrayList<>(onslaught.manifestedPermanentIds());
            if (entry.getChosenPermanentId() != null) {
                manifestedPermanentIds.add(entry.getChosenPermanentId());
            }
            entry.setChosenPermanentId(null);
            onslaught = new ManifestDreadXTimesThenPutCountersEffect(
                    onslaught.amount(),
                    onslaught.remainingManifestations() - 1,
                    onslaught.counterCount(),
                    manifestedPermanentIds,
                    false);
            replaceCurrentEffect(entry, effect, onslaught);
        }

        if (onslaught.remainingManifestations() <= 0) {
            putCountersOnManifestedPermanents(gameData, entry, onslaught);
            return;
        }

        ManifestDreadXTimesThenPutCountersEffect waiting = new ManifestDreadXTimesThenPutCountersEffect(
                onslaught.amount(),
                onslaught.remainingManifestations(),
                onslaught.counterCount(),
                onslaught.manifestedPermanentIds(),
                true);
        replaceCurrentEffect(entry, onslaught, waiting);
        gameData.rerunCurrentEffectAfterInteraction = true;
        manifestDreadEffectHandler.resolve(gameData, entry, ManifestDreadEffect.forController());

        if (!gameData.interaction.isAwaitingInput()) {
            replaceCurrentEffect(entry, waiting, new ManifestDreadXTimesThenPutCountersEffect(
                    waiting.amount(), 0, waiting.counterCount(), waiting.manifestedPermanentIds(), false));
            putCountersOnManifestedPermanents(gameData, entry, waiting);
            gameData.rerunCurrentEffectAfterInteraction = false;
        }
    }

    private void putCountersOnManifestedPermanents(
            GameData gameData, StackEntry entry, ManifestDreadXTimesThenPutCountersEffect effect) {
        for (UUID permanentId : effect.manifestedPermanentIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, permanent, CounterType.PLUS_ONE_PLUS_ONE, effect.counterCount());
            }
        }
    }

    private void replaceCurrentEffect(StackEntry entry, CardEffect current, CardEffect replacement) {
        for (int index = 0; index < entry.getEffectsToResolve().size(); index++) {
            if (entry.getEffectsToResolve().get(index) == current) {
                entry.replaceEffectToResolve(index, replacement);
                return;
            }
        }
        throw new IllegalStateException("Current effect is not present on the stack entry");
    }
}
