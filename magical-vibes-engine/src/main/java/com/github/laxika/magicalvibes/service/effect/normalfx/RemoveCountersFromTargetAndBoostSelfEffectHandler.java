package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetAndBoostSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * "Remove up to X counters from target permanent. For each counter removed this way, this creature
 * gets +1/+0 until end of turn." The controller chooses how many counters to remove, and of which
 * kinds when the target has several. With one kind of counter the number is picked here and this
 * handler re-runs with {@code chosenXValue}; with several kinds the per-kind choice (which also
 * applies the boost once the last kind is answered) is driven by the counter-kind choice.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCountersFromTargetAndBoostSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCountersFromTargetAndBoostSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Integer chosenAmount = gameData.chosenXValue;
        gameData.chosenXValue = null;

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        List<CounterType> kinds = new ArrayList<>();
        int available = 0;
        for (CounterType type : CounterType.values()) {
            if (type == CounterType.ANY || type == CounterType.SILVER) {
                continue;
            }
            int count = target.getCounterCount(type);
            if (count > 0) {
                kinds.add(type);
                available += count;
            }
        }
        int limit = Math.min(Math.max(0, entry.getXValue()), available);
        if (limit <= 0) {
            return;
        }

        if (kinds.size() > 1) {
            playerInputService.beginRemoveCountersOfKindChoice(gameData, entry.getControllerId(),
                    target.getId(), entry.getCard().getName(),
                    kinds.stream().map(CounterType::name).toList(), 0, limit,
                    Math.min(limit, target.getCounterCount(kinds.getFirst())),
                    entry.getSourcePermanentId(), 0);
            return;
        }

        if (chosenAmount == null) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                    entry.getControllerId(), limit,
                    "Choose how many counters to remove from " + target.getCard().getName()
                            + " (0-" + limit + ").",
                    entry.getCard().getName()));
            return;
        }

        CounterType type = kinds.getFirst();
        int removed = Math.max(0, Math.min(chosenAmount, limit));
        if (removed <= 0) {
            return;
        }
        target.setCounterCount(type, target.getCounterCount(type) - removed);
        gameLogService.append(gameData, GameLog.textCardText(removed + " counter(s) removed from ",
                target.getCard(), "."));
        log.info("Game {} - {} counter(s) removed from {}", gameData.id, removed, target.getCard().getName());

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            source.setPowerModifier(source.getPowerModifier() + removed);
            gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                    .text(" gets +" + removed + "/+0 until end of turn.").build());
            log.info("Game {} - {} gets +{}/+0", gameData.id, source.getCard().getName(), removed);
        }
    }
}
