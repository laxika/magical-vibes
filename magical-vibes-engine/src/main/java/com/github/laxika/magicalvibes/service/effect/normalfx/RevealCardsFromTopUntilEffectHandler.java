package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shows everybody the cards revealed (CR 701.20a) without moving any and records {@link StepKind#REVEAL}. It never
 * prompts.
 */
@Component
@RequiredArgsConstructor
public class RevealCardsFromTopUntilEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealCardsFromTopUntilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reveal = (RevealCardsFromTopUntilEffect) effect;
        AmountContext context = AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot());
        int wanted = amountEvaluationService.evaluate(gameData, reveal.matches(), context);
        int most = reveal.atMost() == null ? Integer.MAX_VALUE
                : amountEvaluationService.evaluate(gameData, reveal.atMost(), context);
        CardPredicate until = amountEvaluationService.bindAmounts(gameData, reveal.until(), context);
        List<UUID> revealedIds = new ArrayList<>();
        List<UUID> playerIds = new ArrayList<>();
        List<MovedCard> whereTheyAre = new ArrayList<>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, reveal, reveal.library())) {
            String playerName = gameData.playerIdToName.get(playerId);
            List<Card> library = gameData.playerDecks.getOrDefault(playerId, List.of());
            if (library.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
                continue;
            }
            List<Card> revealed = new ArrayList<>();
            int found = 0;
            for (Card card : library) {
                if (found >= wanted || revealed.size() >= most) {
                    break;
                }
                revealed.add(card);
                // Judged where it is: an ability that changes cards in a library still applies to it.
                if (predicateEvaluationService.matchesCardPredicate(card, until, entry.getCard().getId(), gameData,
                        playerId, entry.getSourcePermanentId(), null, entry.getXValue(),
                        entry.getSourcePermanentSnapshot())) {
                    found++;
                }
            }
            if (revealed.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " reveals no cards."));
                continue;
            }
            playerIds.add(playerId);
            GameLog.Builder shown = GameLog.builder().text(playerName + " reveals ");
            for (int i = 0; i < revealed.size(); i++) {
                Card card = revealed.get(i);
                revealedIds.add(card.getId());
                whereTheyAre.add(new MovedCard(card.getId(), card.getId(), Zone.LIBRARY, playerId));
                if (i > 0) {
                    shown.text(", ");
                }
                shown.card(card);
            }
            gameLogService.append(gameData, shown.text(" from the top of their library.").build());
            if (found < wanted && revealed.size() == library.size()) {
                gameLogService.append(gameData, GameLog.text(playerName + " reveals their entire library."));
            }
        }
        entry.recordStep(StepKind.REVEAL, StepOutcome.ofObjects(revealedIds, playerIds, whereTheyAre));
    }
}
