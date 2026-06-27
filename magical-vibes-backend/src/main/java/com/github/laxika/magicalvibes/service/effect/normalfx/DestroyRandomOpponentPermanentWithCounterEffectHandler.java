package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyRandomOpponentPermanentWithCounterEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyRandomOpponentPermanentWithCounterEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyRandomOpponentPermanentWithCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyRandomOpponentPermanentWithCounterEffect) effect;
        UUID controllerId = entry.getControllerId();
                CounterType counterType = e.counterType();

                // Find all permanents opponents control with the specified counter
                List<Permanent> candidates = new ArrayList<>();
                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (playerId.equals(controllerId)) continue;
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield == null) continue;
                    for (Permanent perm : battlefield) {
                        int counterCount = switch (counterType) {
                            case AIM -> perm.getCounterCount(CounterType.AIM);
                            case CHARGE -> perm.getCounterCount(CounterType.CHARGE);
                            default -> 0;
                        };
                        if (counterCount > 0) {
                            candidates.add(perm);
                        }
                    }
                }

                // Re-check intervening-if: need at least minRequired permanents with counters
                if (candidates.size() < e.minRequired()) {
                    log.info("Game {} - {} end step trigger fizzles — only {} permanents with {} counters (need {})",
                            gameData.id, entry.getCard().getName(), candidates.size(),
                            counterType.name().toLowerCase(), e.minRequired());
                    return;
                }

                // Destroy one at random
                int randomIndex = ThreadLocalRandom.current().nextInt(candidates.size());
                Permanent chosen = candidates.get(randomIndex);
                destructionSupport.tryDestroyAndLog(gameData, chosen, entry.getCard().getName(), false);
    }
}
