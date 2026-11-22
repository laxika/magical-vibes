package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resolves {@link ExileCardAtRandomEffect}: each named player, in APNAP order, exiles one matching card of their
 * graveyard or library picked at random (the library is not shuffled). A graveyard card is recorded as it last existed
 * there (CR 608.2h). Records the {@link StepKind#EXILE} step (the cards and their owners, or nothing). It never prompts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileCardAtRandomEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardAtRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var random = (ExileCardAtRandomEffect) effect;
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        boolean fromGraveyard = random.zone() == Zone.GRAVEYARD;
        boolean fromHand = random.zone() == Zone.HAND;
        String zoneName = fromGraveyard ? "graveyard" : fromHand ? "hand" : "library";
        Integer eventValue = entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY ? entry.getEventValue() : null;
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, random.player())) {
            String playerName = gameData.playerIdToName.get(playerId);
            List<Card> zone = fromGraveyard ? gameData.playerGraveyards.get(playerId)
                    : fromHand ? gameData.playerHands.get(playerId) : gameData.playerDecks.get(playerId);
            List<Card> candidates = zone == null ? List.of() : zone.stream()
                    .filter(card -> random.filter() == null || predicateEvaluationService.matchesCardPredicate(card,
                            random.filter(), entry.getCard().getId(), gameData, playerId,
                            entry.getSourcePermanentId(), entry.getTriggeringPermanentPowerAtTrigger(),
                            entry.getXValue(), entry.getSourcePermanentSnapshot(), eventValue))
                    .toList();
            if (candidates.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " has no " + (random.filter() == null
                        ? "card" : "matching card") + " in their " + zoneName + " to exile."));
                continue;
            }
            List<Card> picked = new ArrayList<>(candidates);
            Collections.shuffle(picked, ThreadLocalRandom.current());
            for (Card exiled : picked.subList(0, Math.min(random.count(), picked.size()))) {
                List<LastKnownCardStats> known = List.of();
                if (fromGraveyard) {
                    known = gameQueryService.asInGraveyard(gameData, List.of(exiled));
                    permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiled.getId());
                } else if (!zone.removeIf(card -> card.getId().equals(exiled.getId()))) {
                    continue;
                }
                exileService.exileCard(gameData, playerId, exiled);
                entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(exiled.getId()), List.of(playerId))
                        .withLastKnownCards(known));
                gameLogService.append(gameData, GameLog.builder()
                        .text(playerName + " exiles ").card(exiled)
                        .text(" at random from their " + zoneName + ".").build());
                log.info("Game {} - {} exiles {} at random from their {}", gameData.id, playerName, exiled.getName(),
                        zoneName);
            }
        }
    }
}
