package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscardSpecificCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardSpecificCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardSpecificCardEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }
        List<UUID> named = e.cards() == null ? List.of(e.cardId())
                : subjectResolutionService.cards(gameData, entry, e, e.cards()).stream().map(Card::getId).toList();
        List<Card> discarded = hand.stream().filter(c -> named.contains(c.getId())).toList();
        if (discarded.isEmpty()) {
            return;
        }

        hand.removeAll(discarded);
        gameData.discardCausedByOpponent = false;
        gameData.beginDiscardEvent(entry.getControllerId());
        try {
            for (Card card : discarded) {
                graveyardService.discardCard(gameData, entry.getControllerId(), card);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(entry.getControllerId()) + " discards ", card, "."));
                gameData.discarded(entry.getControllerId(), card);
            }
        } finally {
            gameData.finishDiscardEvent();
        }

    }
}
