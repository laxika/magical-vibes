package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConjureCardNamedIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardNamedIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardNamedIntoLibraryEffect conjure = (ConjureCardNamedIntoLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null) {
            return;
        }

        int conjuredCount = 0;
        for (int i = 0; i < conjure.count(); i++) {
            Card card = createCardByName(conjure.cardName());
            if (card == null) {
                break;
            }

            card.setOwnerId(controllerId);
            card.freeze();
            library.add(card);
            conjuredCount++;
        }

        if (conjuredCount == 0) {
            return;
        }

        triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, controllerId, conjuredCount);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures " + conjuredCount + " card" + (conjuredCount == 1 ? "" : "s")
                        + " named " + conjure.cardName() + " into "
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
