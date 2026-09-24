package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConjureCardNamedIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardNamedIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardNamedIntoHandEffect conjure = (ConjureCardNamedIntoHandEffect) effect;
        do {
            Card card = createCardByName(conjure.cardName());
            if (card == null) {
                return;
            }

            card.setOwnerId(entry.getControllerId());
            card.freeze();
            gameData.addCardToHand(entry.getControllerId(), card);
            if (conjure.discardAtNextEndStep()) {
                gameData.queueDelayedAction(new DelayedEndStepTrigger(
                        entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), null,
                        new DiscardSpecificCardEffect(card.getId())));
            }
            gameLogService.append(gameData, GameLog.cardThen(card, " is conjured into "
                    + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
        } while (conjure.repeatUntilHandSize() > 0
                && gameData.playerHands.getOrDefault(entry.getControllerId(), java.util.List.of()).size()
                < conjure.repeatUntilHandSize());
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
