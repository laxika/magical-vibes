package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link ExileCardsFromTopUntilEffect}: each named player, in APNAP order, exiles cards from the top of their
 * library one at a time (each its own zone-change event) until one matches the effect's card filter or the library is
 * empty, each exiled with the source permanent when the effect says so. The card filter is bound as the step begins
 * ({@code SubjectResolutionService.bindResolutionFilter}): a spell-relative stop ("shares a card type with it", "a
 * different name than that spell") reads the spell as it is or last existed (CR 608.2h). Records the {@link StepKind#EXILE} step: every card exiled and its owner, or nothing. It never prompts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileCardsFromTopUntilEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardsFromTopUntilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var until = (ExileCardsFromTopUntilEffect) effect;
        // "Shares a card type with it", "a different name than that spell": read once, as the step begins (CR 608.2h).
        CardPredicate stop = subjectResolutionService.bindResolutionFilter(gameData, entry, effect, until.until());
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        // "Exiled with [this]" (CR 607.2a): linked to the source permanent.
        UUID linkedSourceId = until.linkedToSource() ? entry.getSourcePermanentId() : null;
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, until.library())) {
            List<Card> library = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (library == null || library.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
                continue;
            }
            Card hit = null;
            int exiledCount = 0;
            int exiledManaValue = 0;
            while (!library.isEmpty()) {
                Card top = library.removeFirst();
                gameData.rulesEvents.separately(() -> {
                    if (linkedSourceId != null) {
                        exileService.exileCard(gameData, playerId, top, linkedSourceId);
                    } else {
                        exileService.exileCard(gameData, playerId, top);
                    }
                });
                entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(top.getId()), List.of(playerId)));
                exiledCount++;
                if (until.until() == null) {
                    exiledManaValue = SaturatingMath.add(exiledManaValue, top.getManaValue());
                    if (exiledManaValue >= until.totalManaValue()) {
                        hit = top;
                        break;
                    }
                } else if (predicateEvaluationService.matchesCardPredicate(top, stop, entry.getCard().getId(),
                        gameData, playerId)) {
                    hit = top;
                    break;
                }
            }
            if (hit == null) {
                gameLogService.append(gameData, GameLog.text(playerName + " exiles " + exiledCount
                        + " card(s) from the top of their library, but none stops it."));
                continue;
            }
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles " + exiledCount + " card(s) from the top of their library until ")
                    .card(hit).text(".").build());
            log.info("Game {} - {} exiles {} card(s) until {}", gameData.id, playerName, exiledCount, hit.getName());
        }
    }
}
