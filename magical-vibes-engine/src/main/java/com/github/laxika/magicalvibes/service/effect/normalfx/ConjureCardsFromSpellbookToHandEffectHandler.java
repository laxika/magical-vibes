package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves an exact-size spellbook choice that conjures all selected cards into hand. */
@Component
@RequiredArgsConstructor
public class ConjureCardsFromSpellbookToHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardsFromSpellbookToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardsFromSpellbookToHandEffect conjure =
                (ConjureCardsFromSpellbookToHandEffect) effect;
        List<Card> spellbookCards = new ArrayList<>();
        for (var reference : conjure.spellbook()) {
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
                "Choose " + conjure.count() + " cards from the spellbook.",
                DraftFromSpellbookEffect.DraftMode.CONJURE_TO_HAND,
                conjure.count(), conjure.count(), conjure.chosenCardThenEffect()));
    }
}
