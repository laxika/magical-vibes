package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a spellbook draft by offering three random cards for a hand choice. */
@Component
@RequiredArgsConstructor
public class DraftCardFromSpellbookToHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftCardFromSpellbookToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DraftCardFromSpellbookToHandEffect draft = (DraftCardFromSpellbookToHandEffect) effect;
        List<Card> spellbookCards = new ArrayList<>();
        for (DraftCardFromSpellbookToHandEffect.CardPrintingReference reference : draft.spellbook()) {
            CardSet set = CardSet.findByCode(reference.setCode());
            if (set == null) {
                throw new IllegalArgumentException("Unknown card set: " + reference.setCode());
            }

            CardPrinting printing = cardCatalog.findByCollectorNumber(set, reference.collectorNumber());
            Card card = printing.createCard();
            card.setOwnerId(entry.getControllerId());
            card.freeze();
            spellbookCards.add(card);
        }

        Collections.shuffle(spellbookCards, ThreadLocalRandom.current());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookCardChoice(
                entry.getControllerId(), spellbookCards.subList(0, 3),
                "Choose a card from " + entry.getCard().getName() + "'s spellbook."));
    }
}
