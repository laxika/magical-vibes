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
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Resolves a card conjured into its controller's library. */
@Component
@RequiredArgsConstructor
public class ConjureCardIntoControllerLibraryEffectHandler implements NormalEffectHandlerBean {

    private static final int TOP_CARDS_DEPTH = 15;

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

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
        conjuredCard.setConjured(true);
        conjuredCard.setOwnerId(entry.getControllerId());
        conjure.additionalAbilities().forEach(conjuredCard::addAbility);
        conjuredCard.freeze();

        var library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }
        library.add(ThreadLocalRandom.current().nextInt(Math.min(TOP_CARDS_DEPTH, library.size() + 1)), conjuredCard);

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a " + conjuredCard.getName() + " card into the top fifteen cards of their library."));
        triggerCollectionService.checkConjureTriggers(gameData, entry.getControllerId(), 1);
    }
}
