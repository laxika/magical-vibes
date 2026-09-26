package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random spellbook card conjured into exile with a temporary play permission. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect conjure =
                (ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect) effect;
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
        exileService.exileCard(gameData, entry.getControllerId(), conjuredCard);
        exileSupport.grantPlayUntilOwnersNextTurn(
                gameData, conjuredCard.getId(), entry.getControllerId());

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a " + conjuredCard.getName()
                        + " card into exile (may play until end of next turn)."));
    }
}
