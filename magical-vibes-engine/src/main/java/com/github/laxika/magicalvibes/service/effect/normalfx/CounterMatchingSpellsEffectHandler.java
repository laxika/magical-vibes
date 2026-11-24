package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterMatchingSpellsEffect;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Counters every spell or ability on the stack of the effect's kinds matching its {@code StackEntryPredicate}
 * ({@link CounterMatchingSpellsEffect}). Snapshot-then-counter because removing mutates the stack; each one is
 * looked up again and skipped when it left or can't be countered. Records the number countered as the entry's
 * event value and the countered objects as the COUNTER_SPELL step.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounterMatchingSpellsEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final TargetLegalityService targetLegalityService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterMatchingSpellsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterMatchingSpellsEffect matching = (CounterMatchingSpellsEffect) effect;
        entry.recordStep(StepKind.COUNTER_SPELL, StepOutcome.NONE);

        List<UUID> toCounter = new ArrayList<>();
        for (StackEntry se : gameData.stack) {
            if (se == entry || !matching.types().contains(se.getEntryType())) {
                continue;
            }
            if (!targetLegalityService.matchesStackEntryPredicate(
                    gameData, se, matching.filter(), entry.getControllerId())) {
                continue;
            }
            toCounter.add(se.getTargetableId());
        }

        List<UUID> counteredIds = new ArrayList<>();
        List<UUID> controllers = new ArrayList<>();
        for (UUID targetId : toCounter) {
            StackEntry resolved = counterSupport.findCounterTargetExcludingSource(gameData, targetId, entry);
            if (resolved != null && counterSupport.counterSpell(gameData, entry, resolved)) {
                counteredIds.add(targetId);
                if (resolved.getControllerId() != null && !controllers.contains(resolved.getControllerId())) {
                    controllers.add(resolved.getControllerId());
                }
            }
        }
        entry.setEventValue(counteredIds.size());
        if (!counteredIds.isEmpty()) {
            entry.recordStep(StepKind.COUNTER_SPELL, StepOutcome.ofObjects(counteredIds, controllers));
        }
        log.info("Game {} - {} countered {} spells or abilities",
                gameData.id, entry.getCard().getName(), counteredIds.size());
    }
}
