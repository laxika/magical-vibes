package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsIntoTopOfLibrariesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Resolves digital cards conjured into players' libraries. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConjureCardsIntoTopOfLibrariesEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardsIntoTopOfLibrariesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardsIntoTopOfLibrariesEffect e = (ConjureCardsIntoTopOfLibrariesEffect) effect;
        CardSet set = CardSet.findByCode(e.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set: " + e.setCode());
        }

        CardPrinting printing = cardCatalog.findByCollectorNumber(set, e.collectorNumber());
        String cardName = printing.createCard().getName();
        int totalConjured = 0;
        for (var playerId : gameData.playerIds) {
            var library = gameData.playerDecks.get(playerId);
            if (library == null) {
                continue;
            }

            for (int i = 0; i < e.amount(); i++) {
                Card conjured = printing.createCard();
                conjured.setOwnerId(playerId);
                conjured.freeze();
                library.add(0, conjured);
                totalConjured++;
            }
            shuffleTopCards(library, e.depth());
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures " + e.amount() + " " + cardName + " card(s) into the top "
                        + e.depth() + " cards of each player's library."));
        log.info("Game {} - {} conjures {} {} card(s) into the top {} cards of each library",
                gameData.id, entry.getCard().getName(), totalConjured, cardName, e.depth());
    }

    private void shuffleTopCards(java.util.List<Card> library, int depth) {
        int shuffleSize = Math.min(depth, library.size());
        for (int i = shuffleSize - 1; i > 0; i--) {
            int swapIndex = ThreadLocalRandom.current().nextInt(i + 1);
            Card card = library.get(i);
            library.set(i, library.get(swapIndex));
            library.set(swapIndex, card);
        }
    }
}
