package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoControllerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Resolves a card conjured into its controller's library. */
@Component
@RequiredArgsConstructor
public class ConjureCardIntoControllerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardIntoControllerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardIntoControllerLibraryEffect conjure =
                (ConjureCardIntoControllerLibraryEffect) effect;
        CardSet set = CardSet.findByCode(conjure.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set: " + conjure.setCode());
        }

        CardPrinting printing = cardCatalog.findByCollectorNumber(set, conjure.collectorNumber());
        Card conjuredCard = printing.createCard();
        conjuredCard.setOwnerId(entry.getControllerId());
        conjure.additionalEffects().forEach((slot, effects) ->
                effects.forEach(additionalEffect -> conjuredCard.addEffect(slot, additionalEffect)));
        conjuredCard.freeze();

        var library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }
        library.add(0, conjuredCard);
        shuffleTopCards(library, 15);

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a " + conjuredCard.getName() + " card into the top fifteen cards of their library."));
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
