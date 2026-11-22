package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnExiledCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Resolves {@link PutCounterOnExiledCardsEffect}: marks each card its subject names in exile in that counter's store.
 * A fetch counter also records the effect's controller as the card's exiler. It never prompts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnExiledCardsEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnExiledCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (PutCounterOnExiledCardsEffect) effect;
        String counterName = put.counter().name().toLowerCase(Locale.ROOT);
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, put, put.cards())) {
            if (located.zone() != Zone.EXILE) {
                continue;
            }
            Card card = located.card();
            switch (put.counter()) {
                case MEMORY -> gameData.exiledCardsWithMemoryCounters.add(card.getId());
                case BRAIN -> gameData.exiledCardsWithBrainCounters.add(card.getId());
                case CROAK -> gameData.exiledCardsWithCroakCounters.add(card.getId());
                case FETCH -> gameData.markExiledCardWithFetchCounter(card.getId(), entry.getControllerId());
                case TAKEOVER -> gameData.exiledCardsWithTakeoverCounters.add(card.getId());
            }
            gameLogService.append(gameData, GameLog.cardThen(card, " gets a " + counterName + " counter."));
            log.info("Game {} - {} in exile gets a {} counter", gameData.id, card.getName(), counterName);
        }
    }
}
