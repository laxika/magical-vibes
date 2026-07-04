package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
public class ProliferateEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProliferateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        // Collect all permanents with counters (any player's battlefield)
        List<UUID> eligiblePermanentIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0
                    || p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                    || p.getCounterCount(CounterType.LOYALTY) > 0
                    || p.getCounterCount(CounterType.SLIME) > 0
                    || p.getCounterCount(CounterType.HATCHLING) > 0
                    || p.getCounterCount(CounterType.AIM) > 0) {
                eligiblePermanentIds.add(p.getId());
            }
        });

        if (eligiblePermanentIds.isEmpty()) {
            String logEntry = "Proliferate: no permanents with counters to choose.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Proliferate: no eligible permanents", gameData.id);
            return;
        }

        // Count total proliferate effects in this stack entry (e.g. "proliferate, then proliferate again")
        // so the handler knows how many rounds of choices remain after this one.
        long totalProliferates = entry.getEffectsToResolve().stream()
                .filter(e -> e instanceof ProliferateEffect)
                .count();
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligiblePermanentIds,
                eligiblePermanentIds.size(),
                new MultiPermanentChoiceContext.Proliferate((int) totalProliferates),
                "Proliferate: Choose permanents to add counters to.");
    }
}
