package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random spellbook card conjured into exile with a temporary free-cast permission. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect) effect;
        List<Card> availableCards = conjure.spellbook().stream()
                .map(this::createCardIfImplemented)
                .filter(card -> card != null
                        && (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)))
                .toList();
        if (availableCards.isEmpty()) {
            return;
        }

        Card conjuredCard = availableCards.get(ThreadLocalRandom.current().nextInt(availableCards.size()));
        conjuredCard.setOwnerId(entry.getControllerId());
        conjuredCard.freeze();
        exileService.exileCard(gameData, entry.getControllerId(), conjuredCard);
        gameData.exilePlayPermissions.put(conjuredCard.getId(), entry.getControllerId());
        gameData.exilePlayPermissionsExpireEndOfTurn.add(conjuredCard.getId());
        gameData.exilePlayWithoutPayingManaCost.add(conjuredCard.getId());

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a " + conjuredCard.getName()
                        + " card into exile (may cast without paying its mana cost until end of turn)."));
    }

    private Card createCardIfImplemented(
            ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.CardPrintingReference reference) {
        CardSet set = CardSet.findByCode(reference.setCode());
        if (set == null) {
            return null;
        }
        try {
            CardPrinting printing = cardCatalog.findByCollectorNumber(set, reference.collectorNumber());
            return printing.createCard();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
