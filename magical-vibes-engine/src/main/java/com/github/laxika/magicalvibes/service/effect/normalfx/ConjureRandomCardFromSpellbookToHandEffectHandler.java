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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random spellbook card conjured into hand and publicly revealed. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCardFromSpellbookToHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCardFromSpellbookToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureRandomCardFromSpellbookToHandEffect conjure =
                (ConjureRandomCardFromSpellbookToHandEffect) effect;
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

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(conjuredCard)
                .text(" conjured by ")
                .card(entry.getCard())
                .text(".")
                .build());
        cardRevealService.revealToAllPlayers(
                gameData, entry.getControllerId(),
                com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND,
                java.util.List.of(conjuredCard));
    }
}
