package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DraftCardFromSpellbookToExileEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftCardFromSpellbookToExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() != null) {
            return;
        }

        DraftCardFromSpellbookToExileEffect draft = (DraftCardFromSpellbookToExileEffect) effect;
        List<Card> spellbook = draft.cardNames().stream()
                .map(this::findCard)
                .filter(card -> card != null)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (spellbook.isEmpty()) {
            return;
        }

        Collections.shuffle(spellbook);
        List<Card> offeredCards = new ArrayList<>(spellbook.subList(0, Math.min(3, spellbook.size())));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookDraftToExileChoice(
                entry.getControllerId(), entry.getSourcePermanentId(), offeredCards,
                entry.getCard().getName()));
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
        return null;
    }
}
