package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftCardFromSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftCardFromSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var draft = (DraftCardFromSpellbookEffect) effect;
        List<Card> spellbook = new ArrayList<>();
        for (String cardName : draft.cardNames()) {
            spellbook.add(findCard(cardName));
        }
        if (spellbook.isEmpty()) {
            return;
        }

        Collections.shuffle(spellbook);
        List<Card> offeredCards = new ArrayList<>(spellbook.subList(0, Math.min(3, spellbook.size())));
        UUID controllerId = entry.getControllerId();
        offeredCards.forEach(card -> card.setOwnerId(controllerId));
        List<UUID> offeredCardIds = offeredCards.stream().map(Card::getId).toList();
        boolean selectedToBattlefield = draft.destination() == LibrarySearchDestination.BATTLEFIELD;

        PendingInteraction.LibraryRevealChoice choice = new PendingInteraction.LibraryRevealChoice(
                controllerId, offeredCards, offeredCardIds,
                false, !selectedToBattlefield, false, false, true,
                0, null, 1, "Choose a card from this spellbook.",
                false, 1, false, null,
                false, false, false, draft.battlefieldSelectionFollowUp(),
                false, false, false, false, null,
                false, null);
        if (!selectedToBattlefield) {
            choice = choice.withSelectedCardFollowUp(
                    new CardTruePredicate(),
                    new PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect(offeredCardIds));
        }
        interactionHandlerRegistry.begin(gameData, choice);
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
        throw new IllegalStateException("Cannot draft unimplemented card: " + cardName);
    }
}
