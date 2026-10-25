package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseSameNameToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport.TopCardsResult;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Resolves Stroke of Luck's top-library choice and its same-name continuation. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsChooseSameNameToHandEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsChooseSameNameToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsChooseSameNameToHandEffect e =
                (LookAtTopCardsChooseSameNameToHandEffect) effect;
        if (!e.lookedAtCards().isEmpty()) {
            resolveChosenName(gameData, entry, e.lookedAtCards());
            return;
        }

        TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, e.count(), false);
        if (result == null) {
            return;
        }

        List<Card> topCards = result.topCards();
        if (topCards.size() == 1) {
            gameData.addCardToHand(result.controllerId(), topCards.getFirst());
            loseLifeAsNextStep(entry, 1);
            return;
        }

        String prompt = "Choose one of these cards. Put all cards with the same name into your hand. "
                + "Put the rest on the bottom of your library in a random order.";
        LibrarySearchFollowUp followUp = LibrarySearchFollowUp.forSelectedCardWithRandomRest(
                new CardTruePredicate(),
                LookAtTopCardsChooseSameNameToHandEffect.continuation(topCards));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(result.controllerId(), topCards)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .followUp(followUp)
                        .build(),
                prompt,
                false));
    }

    private void resolveChosenName(GameData gameData, StackEntry entry, List<Card> lookedAtCards) {
        Card chosenCard = entry.getChosenObjectCard();
        if (chosenCard == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        int cardsPutIntoHand = 0;
        for (Card card : lookedAtCards) {
            if (!Objects.equals(card.getName(), chosenCard.getName())) {
                continue;
            }
            if (card.getId().equals(chosenCard.getId())) {
                cardsPutIntoHand++;
            } else if (deck.removeIf(deckCard -> deckCard.getId().equals(card.getId()))) {
                gameData.addCardToHand(controllerId, card);
                cardsPutIntoHand++;
            }
        }

        loseLifeAsNextStep(entry, cardsPutIntoHand);
    }

    /**
     * CR 608.2c: the loss follows putting the cards into hand. As a step of its own, a Lich's Duel Mastery sacrifice
     * replacing it asks its CR 616.1 questions without moving any card again.
     */
    private static void loseLifeAsNextStep(StackEntry entry, int amount) {
        if (amount > 0) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new LoseLifeEffect(amount, LoseLifeRecipient.CONTROLLER)));
        }
    }
}
