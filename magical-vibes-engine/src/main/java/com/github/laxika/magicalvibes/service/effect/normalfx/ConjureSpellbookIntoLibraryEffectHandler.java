package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureSpellbookIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a fixed spellbook conjured into the controller's library. */
@Component
@RequiredArgsConstructor
public class ConjureSpellbookIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureSpellbookIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureSpellbookIntoLibraryEffect conjure = (ConjureSpellbookIntoLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null) {
            return;
        }

        int conjuredCount = 0;
        for (String cardName : conjure.cardNames()) {
            for (int i = 0; i < conjure.copies(); i++) {
                Card card = createCardByName(cardName);
                if (card == null) {
                    continue;
                }

                card.setOwnerId(controllerId);
                card.freeze();
                library.add(card);
                conjuredCount++;
            }
        }

        if (conjuredCount == 0) {
            return;
        }

        triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, controllerId, conjuredCount);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures " + conjuredCount + " cards from its spellbook into "
                        + gameData.playerIdToName.get(controllerId) + "'s library, then shuffles."));
    }

    private Card createCardByName(String cardName) {
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(set)) {
                Card card = printing.createCard();
                if (cardName.equals(card.getName())) {
                    return card;
                }
            }
        }
        return null;
    }
}
