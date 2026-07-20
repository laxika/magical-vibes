package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Look at (or reveal) the top N cards of your library, may put up to one card of each of two types
 * into your hand, then put the rest to the effect's rest destination — the bottom of the library
 * (Gift of the Gargantuan) or the graveyard (Benefaction of Rhonas). The at-most-one-per-type bound
 * is enforced by running two sequential single-card {@link PendingInteraction.LibrarySearch} picks
 * over the same looked-at cards: first the {@code firstType} card (carrying a
 * {@link LibrarySearchFollowUp.SecondBoundedPick} for the second type), then the {@code secondType}
 * card (which disposes the remaining cards on completion). When no first-type card is present the
 * second pick begins directly; when neither is present the looked-at cards are disposed immediately.
 */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsRevealTwoTypesToHandThenRestEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsRevealTwoTypesToHandThenRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsRevealTwoTypesToHandThenRestEffect e =
                (LookAtTopCardsRevealTwoTypesToHandThenRestEffect) effect;
        boolean toGraveyard = e.restDestination() == LookDestination.GRAVEYARD;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), false);
        if (result == null) return;

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        if (e.reveal()) {
            GameLog.Builder builder = GameLog.builder().text(playerName + " reveals ");
            appendCardList(builder, topCards);
            builder.text(" from the top of their library with ").card(entry.getCard()).text(".");
            gameBroadcastService.logAndBroadcast(gameData, builder.build());
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " looks at the top "
                    + LibraryRevealSupport.pluralCards(topCards.size()) + " of their library."));
        }

        List<Card> firstEligible = topCards.stream().filter(c -> c.hasType(e.firstType())).toList();
        List<Card> secondEligible = topCards.stream().filter(c -> c.hasType(e.secondType())).toList();

        // Nothing eligible — dispose everything.
        if (firstEligible.isEmpty() && secondEligible.isEmpty()) {
            disposeRest(gameData, controllerId, playerName, topCards, toGraveyard);
            return;
        }

        if (!firstEligible.isEmpty()) {
            // First pick: the first type. The second pick runs (and disposes the rest) afterwards.
            beginPick(gameData, controllerId, firstEligible, topCards, promptFor(e.firstType()),
                    LibrarySearchFollowUp.forSecondBoundedPick(e.secondType(), toGraveyard), toGraveyard);
            return;
        }

        // No first-type card among the looked-at cards — go straight to the second pick.
        beginPick(gameData, controllerId, secondEligible, topCards, promptFor(e.secondType()),
                LibrarySearchFollowUp.NONE, toGraveyard);
    }

    private void beginPick(GameData gameData, UUID controllerId, List<Card> eligible,
            List<Card> lookedAtCards, String prompt, LibrarySearchFollowUp followUp, boolean toGraveyard) {
        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(eligible))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.HAND)
                .sourceCards(new ArrayList<>(lookedAtCards))
                .reorderRemainingToBottom(!toGraveyard)
                .restToGraveyard(toGraveyard)
                .shuffleAfterSelection(false)
                .followUp(followUp)
                .prompt(prompt)
                .build();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
    }

    private void disposeRest(GameData gameData, UUID controllerId, String playerName,
            List<Card> cards, boolean toGraveyard) {
        if (!toGraveyard) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, cards);
            return;
        }
        for (Card card : cards) {
            graveyardService.addCardToGraveyard(gameData, controllerId, card);
        }
        GameLog.Builder builder = GameLog.builder().text(playerName + " puts ");
        appendCardList(builder, cards);
        builder.text(" into their graveyard.");
        gameBroadcastService.logAndBroadcast(gameData, builder.build());
    }

    private static String promptFor(CardType type) {
        return "You may reveal a " + type.getDisplayName().toLowerCase() + " card from among them and put it into your hand.";
    }

    /** Appends {@code cards} to {@code builder} as comma-separated card segments. */
    private static void appendCardList(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }
}
