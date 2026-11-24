package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromExiledCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Resolves {@link RemoveCountersFromExiledCardsEffect}: a REMOVE_COUNTERS step over cards in exile, recording the
 * counters removed, the cards and their owners. Time counters come off one at a time through suspend's removal
 * ({@link RemoveTimeCounterFromExiledCardEffectHandler#removeTimeCounter}), the effect's controller removing them, so
 * each is a time-counter-removed-in-exile event and the last one offers the card's suspend cast (CR 702.62a). Any
 * other kind is one counters-removed-in-exile event for each card ("when the last refine counter is removed from this
 * card").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCountersFromExiledCardsEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCountersFromExiledCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var remove = (RemoveCountersFromExiledCardsEffect) effect;
        String counterName = remove.counter().name().toLowerCase(Locale.ROOT);
        int removedTotal = 0;
        List<UUID> cardIds = new ArrayList<>();
        List<UUID> ownerIds = new ArrayList<>();
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, remove, remove.cards())) {
            if (located.zone() != Zone.EXILE) {
                continue;
            }
            Card card = located.card();
            int before = gameData.exiledCardCounterCount(card.getId(), remove.counter());
            int removed = Math.min(before, remove.count());
            if (removed <= 0) {
                continue;
            }
            int remaining = before - removed;
            removedTotal += removed;
            cardIds.add(card.getId());
            if (!ownerIds.contains(located.ownerId())) {
                ownerIds.add(located.ownerId());
            }
            if (remove.counter() == CounterType.TIME) {
                for (int i = 0; i < removed; i++) {
                    removeTimeCounterHandler.removeTimeCounter(gameData, card.getId(), entry.getControllerId());
                }
                continue;
            }
            gameData.setExiledCardCounters(card.getId(), remove.counter(), remaining);
            gameLogService.append(gameData, GameLog.cardThenUnlessHidden(gameData, card, (removed == 1
                    ? " has " + article(counterName) + " " + counterName + " counter removed"
                    : " has " + removed + " " + counterName + " counters removed") + " (" + remaining + " remaining)."));
            gameData.rulesEvents.occur(new CounterChangeEvent(CounterChangeEvent.Kind.COUNTER_REMOVED_IN_EXILE,
                    null, null, card, located.ownerId(), entry.getControllerId(), remove.counter(), remaining, before,
                    false, null));
            log.info("Game {} - {} in exile loses {} {} counter(s), {} remaining", gameData.id, card.getName(),
                    removed, counterName, remaining);
        }
        entry.recordStep(StepKind.REMOVE_COUNTERS, removedTotal == 0
                ? StepOutcome.NONE : new StepOutcome(removedTotal, cardIds, ownerIds));
    }

    private static String article(String word) {
        return "aeiou".indexOf(word.charAt(0)) >= 0 ? "an" : "a";
    }
}
