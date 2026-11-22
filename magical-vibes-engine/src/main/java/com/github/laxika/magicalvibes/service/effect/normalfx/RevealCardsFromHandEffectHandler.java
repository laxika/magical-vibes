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
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Shows cards in hands to exactly who the effect says and records {@link StepKind#REVEAL}. A look's log line names no
 * card, and a look at part of a hand no number of them (CR 402.3, 701.20e). It never prompts.
 */
@Component
@RequiredArgsConstructor
public class RevealCardsFromHandEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealCardsFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var show = (RevealCardsFromHandEffect) effect;
        List<Card> named = show.cards() == null ? null
                : subjectResolutionService.cards(gameData, entry, show, show.cards());
        List<UUID> viewerIds = show.seenBy() == null ? null
                : subjectResolutionService.players(gameData, entry, show, show.seenBy());
        List<UUID> shownIds = new ArrayList<>();
        List<UUID> playerIds = new ArrayList<>();
        List<MovedCard> whereTheyAre = new ArrayList<>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, show, show.player())) {
            List<Card> shown = new ArrayList<>(gameData.playerHands.getOrDefault(playerId, List.of()));
            if (named != null) {
                shown.removeIf(card -> !named.contains(card));
            }
            if (show.atRandom() != null) {
                int count = amountEvaluationService.evaluate(gameData, show.atRandom(),
                        AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot()));
                Collections.shuffle(shown, ThreadLocalRandom.current());
                shown = new ArrayList<>(shown.subList(0, Math.clamp(count, 0, shown.size())));
            }
            if (viewerIds == null) {
                reveal(gameData, show, playerId, shown);
            } else {
                for (UUID viewerId : viewerIds) {
                    look(gameData, show, playerId, viewerId, shown);
                }
            }
            if (!shown.isEmpty()) {
                playerIds.add(playerId);
            }
            for (Card card : shown) {
                shownIds.add(card.getId());
                whereTheyAre.add(gameData.boundToStay(
                        new MovedCard(card.getId(), card.getId(), Zone.HAND, playerId)));
            }
        }
        entry.recordStep(StepKind.REVEAL, StepOutcome.ofObjects(shownIds, playerIds, whereTheyAre));
    }

    private void reveal(GameData gameData, RevealCardsFromHandEffect show, UUID playerId, List<Card> shown) {
        boolean random = show.atRandom() != null;
        if (!random && show.cards() == null) {
            cardRevealService.revealHandToAllPlayers(gameData, playerId);
            return;
        }
        String playerName = gameData.playerIdToName.get(playerId);
        if (shown.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards to reveal."));
            return;
        }
        GameLog.Builder line = GameLog.builder().text(playerName + " reveals ");
        for (int i = 0; i < shown.size(); i++) {
            if (i > 0) {
                line.text(", ");
            }
            line.card(shown.get(i));
        }
        gameLogService.append(gameData, line.text(random ? " at random." : " from their hand.").build());
        cardRevealService.revealToAllPlayers(gameData, playerId, GameEventFact.RevealZone.HAND, shown);
    }

    private void look(GameData gameData, RevealCardsFromHandEffect show, UUID playerId, UUID viewerId,
                      List<Card> shown) {
        boolean random = show.atRandom() != null;
        if (!random && show.cards() == null) {
            cardRevealService.lookAtHand(gameData, viewerId, playerId);
            return;
        }
        String viewerName = gameData.playerIdToName.get(viewerId);
        String playerName = gameData.playerIdToName.get(playerId);
        if (random && gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    viewerName + " looks at " + playerName + "'s hand at random. It is empty."));
            return;
        }
        gameLogService.append(gameData, GameLog.text(viewerName + " looks at "
                + (random ? "a card at random" : "cards") + " in " + playerName + "'s hand."));
        cardRevealService.revealToPlayer(gameData, playerId, GameEventFact.RevealZone.HAND, shown, viewerId);
    }
}
