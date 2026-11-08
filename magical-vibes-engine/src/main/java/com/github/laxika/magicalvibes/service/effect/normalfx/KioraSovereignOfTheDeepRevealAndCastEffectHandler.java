package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KioraSovereignOfTheDeepRevealAndCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KioraSovereignOfTheDeepRevealAndCastEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KioraSovereignOfTheDeepRevealAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "Where X is that spell's mana value": the triggering spell's, as it last existed on the stack once it has
        // left (CR 608.2h).
        int spellManaValue = amountEvaluationService.evaluate(gameData, new TargetSpellManaValue(),
                AmountContext.forStackEntry(entry, null));
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty() || spellManaValue <= 0) {
            return;
        }

        int count = Math.min(spellManaValue, deck.size());
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, count);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " looks at the top " + count + " cards of their library ("
                + entry.getCard().getName() + ")."));

        List<Card> castable = topCards.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .filter(card -> card.getManaValue() < spellManaValue)
                .toList();
        if (castable.isEmpty()) {
            Collections.shuffle(topCards);
            deck.addAll(topCards);
            return;
        }

        // The looked-at cards wait in exile while the free cast is offered, so the cast goes through the
        // exile free-cast flow that pays mandatory additional costs (discard, sacrifice, behold) before the
        // spell reaches the stack; the cards not cast return to the bottom of the library.
        topCards.forEach(card -> gameData.addToExile(controllerId, card));

        String prompt = "You may cast a spell with mana value less than "
                + spellManaValue + " from among the top " + count
                + " cards without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, castable)
                        .reveals(false)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .build(),
                prompt,
                true));
        log.info("Game {} - {} looks at the top {} cards for {}",
                gameData.id, gameData.playerIdToName.get(controllerId), count, entry.getCard().getName());
    }
}
