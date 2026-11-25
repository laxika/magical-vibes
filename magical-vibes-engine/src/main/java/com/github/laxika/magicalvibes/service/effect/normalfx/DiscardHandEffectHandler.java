package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DiscardHandEffect}: each player {@link SubjectResolutionService} names discards their entire hand,
 * in APNAP order. There is no player choice, so nothing is asked before the discards (CR 101.4); every card goes to
 * the graveyard, firing discard triggers per card. A {@code you()} discard records the number of cards discarded as
 * the entry's event value.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardHandEffect) effect;
        String cardName = entry.getCard().getName();
        UUID controllerId = entry.getControllerId();

        if (e.player() instanceof PlayerSubject.You) {
            entry.setEventValue(discardHand(gameData, controllerId, controllerId, cardName));
            return;
        }
        for (UUID playerId : subjectResolutionService.players(gameData, entry, e, e.player())) {
            discardHand(gameData, playerId, controllerId, cardName);
        }
    }

    /**
     * Discards one player's entire hand for a spell or ability {@code controllerId} controls and returns the number
     * of cards discarded. A player an opponent's effects can't cause to discard ("spells and abilities your opponents
     * control can't cause you to discard cards") discards nothing (Tamiyo, Collector of Tales' ruling).
     */
    public int discardHand(GameData gameData, UUID playerId, UUID controllerId, String cardName) {
        String playerName = gameData.playerIdToName.get(playerId);
        if (!playerId.equals(controllerId) && !gameQueryService.canEffectCauseDiscard(gameData, playerId, controllerId)) {
            gameLogService.append(gameData, GameLog.text(playerName + " can't be caused to discard (" + cardName + ")."));
            return 0;
        }
        List<Card> hand = gameData.playerHands.get(playerId);

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards to discard (" + cardName + ")."));
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return 0;
        }

        List<Card> discarded = new ArrayList<>(hand);
        hand.clear();
        gameData.discardCausedByOpponent = !playerId.equals(controllerId);

        gameData.beginDiscardEvent(playerId);
        for (Card card : discarded) {
            graveyardService.discardCard(gameData, playerId, card);
            gameData.discarded(playerId, card);
        }
        gameData.finishDiscardEvent();

        gameLogService.append(gameData, GameLog.text(playerName + " discards their hand ("
                + discarded.size() + " card" + (discarded.size() != 1 ? "s" : "") + ") (" + cardName + ")."));
        log.info("Game {} - {} discards hand of {} cards for {}", gameData.id, playerName, discarded.size(), cardName);
        return discarded.size();
    }
}
