package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachCreatureFirstTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutPlusOnePlusOneCounterOnEachCreatureFirstTargetPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutPlusOnePlusOneCounterOnEachCreatureFirstTargetPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetIds() == null || entry.getTargetIds().isEmpty()) {
            return;
        }

        UUID targetPlayerId = entry.getTargetIds().getFirst();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        // Snapshot creatures before placing counters — applyPlusOnePlusOneCounters may push
        // counter-placement triggers onto the stack, so iterate over a stable copy.
        List<Permanent> creatures = battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .toList();
        for (Permanent p : creatures) {
            // Routes through the shared helper so cantHaveCounters is respected and
            // ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT triggers fire (rules-correct placement).
            permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, p, 1);
        }

        log.info("Game {} - {} puts +1/+1 counter on {} creature(s) target player controls",
                gameData.id, entry.getCard().getName(), creatures.size());
    }
}
