package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsAndMayCastEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link LookAtTopCardsAndMayCastEffect}: evaluates the count and the mana-value bound once (CR 608.2h), shows
 * the top cards to their owner (or to every player when they are revealed) and offers the matching spells in one
 * {@code LibrarySearch} prompt with the {@code CAST_WITHOUT_PAYING} destination, which casts the chosen one from the
 * library during the resolution (CR 608.2g) and puts the rest on the bottom in a random order, or leaves them. The
 * cards stay in the library until then (CR 701.20b). Nothing offered, nothing is asked.
 */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsAndMayCastEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CardRevealService cardRevealService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsAndMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var look = (LookAtTopCardsAndMayCastEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, look.count(), context));
        Integer manaValueBound = look.spellManaValueAtMost() == null ? null
                : amountEvaluationService.evaluate(gameData, look.spellManaValueAtMost(), context);
        if (deck == null || deck.isEmpty() || count == 0) {
            return;
        }

        int looked = Math.min(count, deck.size());
        List<Card> topCards = new ArrayList<>(deck.subList(0, looked));
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = FaceDownVisibility.sourceName(gameData, entry);
        if (look.reveals()) {
            GameLog.Builder line = GameLog.builder().text(playerName + " reveals ");
            for (int i = 0; i < topCards.size(); i++) {
                if (i > 0) line.text(", ");
                line.card(topCards.get(i));
            }
            gameLogService.append(gameData, line.text(" (" + sourceName + ").").build());
            cardRevealService.revealToAllPlayers(gameData, controllerId, GameEventFact.RevealZone.LIBRARY, topCards);
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " looks at the top "
                    + LibraryRevealSupport.pluralCards(looked) + " of their library (" + sourceName + ")."));
            cardRevealService.revealToPlayer(gameData, controllerId, GameEventFact.RevealZone.LIBRARY, topCards,
                    controllerId);
        }

        List<Card> offered = topCards.stream()
                .filter(card -> offersFace(gameData, entry, look, controllerId, card, manaValueBound)
                        || card.isModalDoubleFaced() && card.getBackFaceCard() != null
                        && offersFace(gameData, entry, look, controllerId, card.getBackFaceCard(), manaValueBound))
                .toList();
        if (offered.isEmpty()) {
            if (look.restOnBottom()) {
                deck.subList(0, looked).clear();
                Collections.shuffle(topCards);
                deck.addAll(topCards);
            }
            return;
        }

        String prompt = "You may cast " + (look.castFilter() == null ? "a spell" : "one of these spells")
                + (manaValueBound == null ? "" : " with mana value " + manaValueBound + " or less")
                + " from among the top " + LibraryRevealSupport.pluralCards(looked)
                + " without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, offered)
                        .reveals(look.reveals())
                        .canFailToFind(true)
                        .sourceCards(topCards)
                        .reorderRemainingToBottom(look.restOnBottom())
                        .reorderRemainingToTop(!look.restOnBottom())
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .mayCastManaValueAtMost(manaValueBound)
                        .build(),
                prompt,
                true));
    }

    /**
     * Whether {@code face} may be offered: a nonland face (a land is played, not cast) matching the cast filter, its
     * smallest spell mana value within the bound (CR 709.3a: a split card is cast as one half).
     */
    private boolean offersFace(GameData gameData, StackEntry entry, LookAtTopCardsAndMayCastEffect look,
                               UUID controllerId, Card face, Integer manaValueBound) {
        if (face.hasType(CardType.LAND)) {
            return false;
        }
        if (manaValueBound != null && face.getSmallestSpellManaValue() > manaValueBound) {
            return false;
        }
        return look.castFilter() == null || predicateEvaluationService.matchesCardPredicate(
                face, look.castFilter(), entry.getCard().getId(), gameData, controllerId,
                entry.getSourcePermanentId(), null, null, entry.getSourcePermanentSnapshot());
    }
}
