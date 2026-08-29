package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingAllureOfTheUnknownChoice;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllureOfTheUnknownEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Allure of the Unknown's public top-library reveal and opponent choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AllureOfTheUnknownEffectHandler implements NormalEffectHandlerBean {

    private static final int REVEAL_COUNT = 6;

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllureOfTheUnknownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, REVEAL_COUNT);
        if (result == null) {
            return;
        }

        List<Card> revealedCards = result.topCards();
        GameLog.Builder revealBuilder = GameLog.builder().text(result.playerName() + " reveals ");
        for (int i = 0; i < revealedCards.size(); i++) {
            if (i > 0) {
                revealBuilder.text(", ");
            }
            revealBuilder.card(revealedCards.get(i));
        }
        revealBuilder.text(" from the top of their library with ")
                .card(entry.getCard())
                .text(".");
        gameLogService.append(gameData, revealBuilder.build());

        List<Card> eligibleCards = revealedCards.stream()
                .filter(card -> !card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                .toList();
        if (eligibleCards.isEmpty()) {
            putCardsIntoHand(gameData, result.controllerId(), revealedCards);
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(result.controllerId()))
                .toList();
        if (opponents.isEmpty()) {
            putCardsIntoHand(gameData, result.controllerId(), revealedCards);
            return;
        }

        PermanentChoiceContext.AllureOfTheUnknownOpponentChoice choice =
                new PermanentChoiceContext.AllureOfTheUnknownOpponentChoice(
                        result.controllerId(), revealedCards);
        if (opponents.size() == 1) {
            beginOpponentCardChoice(gameData, opponents.getFirst(), choice, eligibleCards);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(choice);
        playerInputService.beginAnyTargetChoice(gameData, result.controllerId(), List.of(), opponents,
                entry.getCard().getName() + " — choose an opponent to choose a card.");
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId,
            PermanentChoiceContext.AllureOfTheUnknownOpponentChoice choice) {
        List<Card> eligibleCards = choice.revealedCards().stream()
                .filter(card -> !card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                .toList();
        beginOpponentCardChoice(gameData, opponentId, choice, eligibleCards);
    }

    private void beginOpponentCardChoice(GameData gameData, UUID opponentId,
            PermanentChoiceContext.AllureOfTheUnknownOpponentChoice choice,
            List<Card> eligibleCards) {
        List<Card> revealedCards = choice.revealedCards();
        gameData.queueInteraction(new PendingAllureOfTheUnknownChoice(choice.controllerId(), opponentId));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                opponentId, revealedCards, eligibleCards.stream().map(Card::getId).toList(),
                false, false, false, false, false, 0, null, 1,
                "Choose a nonland card to exile. The rest go into its controller's hand.",
                false, 1, false));
    }

    public void completeCardChoice(GameData gameData, List<Card> revealedCards,
            List<UUID> selectedCardIds) {
        PendingAllureOfTheUnknownChoice pending =
                gameData.pollPendingInteraction(PendingAllureOfTheUnknownChoice.class);
        if (pending == null) {
            throw new IllegalStateException("No pending Allure of the Unknown choice");
        }

        UUID chosenId = selectedCardIds.isEmpty()
                ? revealedCards.stream()
                        .filter(card -> !card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                        .findFirst()
                        .orElseThrow()
                        .getId()
                : selectedCardIds.getFirst();
        Card chosenCard = null;
        for (Card card : revealedCards) {
            if (card.getId().equals(chosenId)) {
                exileService.exileCard(gameData, pending.controllerId(), card);
                chosenCard = card;
            } else {
                gameData.addCardToHand(pending.controllerId(), card);
            }
        }

        if (chosenCard != null) {
            Card sourceCard = gameData.pendingEffectResolutionEntry != null
                    ? gameData.pendingEffectResolutionEntry.getCard() : chosenCard;
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    sourceCard,
                    pending.opponentId(),
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                    "Cast " + chosenCard.getName() + " without paying its mana cost?",
                    chosenCard.getId()));
        }
    }

    private void putCardsIntoHand(GameData gameData, UUID controllerId, List<Card> cards) {
        for (Card card : cards) {
            gameData.addCardToHand(controllerId, card);
        }
    }
}
