package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfChosenTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;

/** Resolves Animation Module's counter-kind choice and delegates placement to shared support. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddAnotherCounterOfChosenTypeToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    @Autowired private InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddAnotherCounterOfChosenTypeToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            List<CounterType> counterTypes = new ArrayList<>();
            for (CounterType type : CounterType.values()) {
                if (type != CounterType.ANY && type != CounterType.SILVER
                        && target.getCounterCount(type) > 0) {
                    counterTypes.add(type);
                }
            }
            if (counterTypes.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.cardThen(entry.getCard(), " finds no counters on the target."));
                return;
            }
            playerInputService.beginAddAnotherCounterTypeChoice(gameData, entry.getControllerId(), targetId,
                    entry.getCard().getName(), counterTypes, false);
            return;
        }

        if (gameData.playerIds.contains(targetId)) {
            boolean poisonCounters = gameData.playerPoisonCounters.getOrDefault(targetId, 0) > 0;
            boolean energyCounters = gameData.playerEnergyCounters.getOrDefault(targetId, 0) > 0;
            if (!poisonCounters && !energyCounters) return;
            ChoiceContext.AddAnotherCounterTypeChoice context = new ChoiceContext.AddAnotherCounterTypeChoice(
                    targetId, entry.getControllerId(), entry.getCard().getName(), List.of(),
                    poisonCounters, false, null, energyCounters);
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    entry.getControllerId(), null, null, context, context.options(),
                    entry.getCard().getName() + " — Choose a counter to add another of."));
        }
    }
}
