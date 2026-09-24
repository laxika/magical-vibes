package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a full card copy conjured into its controller's hand. */
@Component
@RequiredArgsConstructor
public class ConjureCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardToHandEffect conjure = (ConjureCardToHandEffect) effect;
        CardSet set = CardSet.findByCode(conjure.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set: " + conjure.setCode());
        }

        CardPrinting printing = cardCatalog.findByCollectorNumber(set, conjure.collectorNumber());
        Card conjuredCard = printing.createCard();
        conjuredCard.setOwnerId(entry.getControllerId());
        conjuredCard.freeze();
        gameData.addCardToHand(entry.getControllerId(), conjuredCard);

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a " + conjuredCard.getName() + " card into their hand."));
    }
}
