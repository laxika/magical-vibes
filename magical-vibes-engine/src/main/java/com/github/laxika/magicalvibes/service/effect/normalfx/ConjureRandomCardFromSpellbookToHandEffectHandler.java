package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random spellbook card conjured into hand and publicly revealed. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCardFromSpellbookToHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCardFromSpellbookToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureRandomCardFromSpellbookToHandEffect conjure =
                (ConjureRandomCardFromSpellbookToHandEffect) effect;
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, conjure.count(), AmountContext.forStackEntry(entry, null)));
        List<Card> conjuredCards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            var reference = conjure.spellbook().get(
                    ThreadLocalRandom.current().nextInt(conjure.spellbook().size()));
            CardSet set = CardSet.findByCode(reference.setCode());
            if (set == null) {
                throw new IllegalArgumentException("Unknown card set: " + reference.setCode());
            }

            CardPrinting printing = cardCatalog.findByCollectorNumber(set, reference.collectorNumber());
            Card conjuredCard = printing.createCard();
            conjuredCard.setOwnerId(entry.getControllerId());
            conjuredCard.freeze();
            gameData.addCardToHand(entry.getControllerId(), conjuredCard);
            conjuredCards.add(conjuredCard);

            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ")
                    .card(conjuredCard)
                    .text(" conjured by ")
                    .card(entry.getCard())
                    .text(".")
                    .build());
        }

        if (!conjuredCards.isEmpty()) {
            cardRevealService.revealToAllPlayers(
                    gameData, entry.getControllerId(),
                    com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND,
                    conjuredCards);
        }
    }
}
