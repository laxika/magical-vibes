package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
public class DestroyOneOfTargetsAtRandomEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyOneOfTargetsAtRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
                if (targetIds == null || targetIds.isEmpty()) {
                    return;
                }

                // Filter to still-valid targets (still on the battlefield)
                List<UUID> validTargetIds = new ArrayList<>();
                for (UUID targetId : targetIds) {
                    if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                        validTargetIds.add(targetId);
                    }
                }

                if (validTargetIds.isEmpty()) {
                    log.info("Game {} - {} random destroy fizzles — all targets have left the battlefield",
                            gameData.id, entry.getCard().getName());
                    return;
                }

                int randomIndex = ThreadLocalRandom.current().nextInt(validTargetIds.size());
                UUID chosenId = validTargetIds.get(randomIndex);
                Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
                if (chosen != null) {
                    destructionSupport.tryDestroyAndLog(gameData, chosen, entry.getCard().getName(), false);
                }
    }
}
