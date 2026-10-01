package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftTwiceFromSpellbookEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftTwiceFromSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftTwiceFromSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DraftTwiceFromSpellbookEffect draft = (DraftTwiceFromSpellbookEffect) effect;
        List<Card> offeredCards = createOfferedCards(entry.getControllerId(), draft.spellbook());
        if (offeredCards.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.DraftTwiceSpellbookChoice(
                entry.getControllerId(), offeredCards, List.of(), draft.spellbook(),
                entry.getCard().getName(), 1));
    }

    public List<Card> createOfferedCards(UUID playerId,
                                         List<DraftFromSpellbookEffect.SpellbookCard> spellbook) {
        List<Card> availableCards = new ArrayList<>();
        for (DraftFromSpellbookEffect.SpellbookCard reference : spellbook) {
            CardSet set = CardSet.findByCode(reference.setCode());
            if (set == null) {
                continue;
            }
            try {
                Card card = cardCatalog.findByCollectorNumber(set, reference.collectorNumber()).createCard();
                card.setOwnerId(playerId);
                card.freeze();
                availableCards.add(card);
            } catch (IllegalArgumentException ignored) {
                // Spellbook cards that are not implemented are omitted from the offer.
            }
        }
        Collections.shuffle(availableCards);
        return new ArrayList<>(availableCards.subList(0, Math.min(3, availableCards.size())));
    }
}
