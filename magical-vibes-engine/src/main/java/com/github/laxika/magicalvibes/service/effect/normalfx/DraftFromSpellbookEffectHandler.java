package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Offers three random implemented cards from a digital spellbook. */
@Component
@RequiredArgsConstructor
public class DraftFromSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftFromSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DraftFromSpellbookEffect draft = (DraftFromSpellbookEffect) effect;
        List<Card> availableCards = draft.spellbook().stream()
                .map(this::createCardIfImplemented)
                .filter(card -> card != null)
                .toList();
        if (availableCards.isEmpty()) {
            return;
        }

        List<Card> offeredCards = new ArrayList<>(availableCards);
        Collections.shuffle(offeredCards);
        offeredCards = new ArrayList<>(offeredCards.subList(0, Math.min(3, offeredCards.size())));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookCardChoice(
                entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId(),
                offeredCards,
                "Choose a card from " + entry.getCard().getName() + "'s spellbook.",
                draft.mode()));
    }

    private Card createCardIfImplemented(DraftFromSpellbookEffect.SpellbookCard reference) {
        CardSet set = CardSet.findByCode(reference.setCode());
        if (set == null) {
            return null;
        }
        try {
            return cardCatalog.findByCollectorNumber(set, reference.collectorNumber()).createCard();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
