package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a spellbook draft that ends in the drafting player's hand. */
@Component
@RequiredArgsConstructor
public class DraftCardIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CardCatalog cardCatalog;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftCardIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card card = cardCatalog.createCard(((DraftCardIntoHandEffect) effect).printing());
        card.setOwnerId(entry.getControllerId());
        card.freeze();
        gameData.addCardToHand(entry.getControllerId(), card);
        // A drafted card goes into a hidden zone (CR 400.2): the line doesn't name it.
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " drafts a card from ", entry.getCard(),
                "'s spellbook into their hand."));
    }
}
