package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

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
        var e = (ConjureCardToHandEffect) effect;
        Card conjuredCard = findCard(e.cardName());
        conjuredCard.setOwnerId(entry.getControllerId());
        if (!e.removedKeywords().isEmpty()) {
            EnumSet<Keyword> keywords = conjuredCard.getKeywords().isEmpty()
                    ? EnumSet.noneOf(Keyword.class)
                    : EnumSet.copyOf(conjuredCard.getKeywords());
            keywords.removeAll(e.removedKeywords());
            conjuredCard.setKeywords(keywords);
        }
        conjuredCard.freeze();
        gameData.addCardToHand(entry.getControllerId(), conjuredCard);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures " + conjuredCard.getName() + " into their hand."));
    }

    private Card findCard(String cardName) {
        Set<String> inspectedClasses = new HashSet<>();
        for (CardSet cardSet : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(cardSet)) {
                if (!inspectedClasses.add(printing.cardClassName())) {
                    continue;
                }
                Card card = printing.createCard();
                if (cardName.equals(card.getName())) {
                    return card;
                }
            }
        }
        throw new IllegalStateException("Cannot conjure unimplemented card: " + cardName);
    }
}
