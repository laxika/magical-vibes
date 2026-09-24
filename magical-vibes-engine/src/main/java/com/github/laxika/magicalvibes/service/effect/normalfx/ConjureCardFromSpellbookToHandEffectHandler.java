package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves a spellbook choice and queues the selected card for conjuring into hand. */
@Component
@RequiredArgsConstructor
public class ConjureCardFromSpellbookToHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardFromSpellbookToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardFromSpellbookToHandEffect conjure =
                (ConjureCardFromSpellbookToHandEffect) effect;
        List<Card> spellbookCards = new ArrayList<>();
        for (ConjureCardFromSpellbookToHandEffect.CardPrintingReference reference : conjure.spellbook()) {
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

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookCardChoice(
                entry.getControllerId(), spellbookCards,
                "Choose a card from Charged Conjuration's spellbook."));
    }
}
