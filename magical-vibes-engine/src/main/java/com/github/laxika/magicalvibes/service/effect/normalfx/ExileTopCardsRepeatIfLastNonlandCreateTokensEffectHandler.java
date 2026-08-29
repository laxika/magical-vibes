package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatIfLastNonlandCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsRepeatIfLastNonlandCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsRepeatIfLastNonlandCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsRepeatIfLastNonlandCreateTokensEffect) effect;
        var library = gameData.playerDecks.get(entry.getControllerId());
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        String sourceName = entry.getCard().getName();
        int nonlandCards = 0;
        boolean repeat = true;

        while (repeat && !library.isEmpty()) {
            int cardsToExile = Math.min(e.cardsPerIteration(), library.size());
            Card lastCard = null;
            for (int i = 0; i < cardsToExile; i++) {
                Card card = library.removeFirst();
                exileService.exileCard(gameData, entry.getControllerId(), card);
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " exiles ", card, " from the top of their library (" + sourceName + ")."));
                lastCard = card;
                if (!card.hasType(CardType.LAND)) {
                    nonlandCards++;
                }
            }

            repeat = cardsToExile == e.cardsPerIteration() && !lastCard.hasType(CardType.LAND);
        }

        if (nonlandCards > 0) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData,
                    entry.getControllerId(),
                    e.tokenTemplate(),
                    nonlandCards,
                    entry.getCard().getSetCode()));
        }

        log.info("Game {} - {} exiled {} nonland cards with {}",
                gameData.id, playerName, nonlandCards, sourceName);
    }
}
