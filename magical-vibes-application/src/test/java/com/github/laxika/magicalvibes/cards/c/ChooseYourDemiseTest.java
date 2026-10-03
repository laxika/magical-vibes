package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChooseYourDemise.class, Forest.class, Island.class, Mountain.class, Plains.class})
class ChooseYourDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("The controller separates four cards and the opponent chooses the hand pile")
    void opponentChoosesPileForHandAndOtherPileGoesToBottom() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island, forest, mountain, plains));

        resolveScheme();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.description()).contains(island.getName(), forest.getName())
                .contains("2 cards");

        harness.handleMayAbilityChosen(player2, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain, plains);
    }

    @Test
    @DisplayName("The face-down pile remains hidden while the opponent chooses it")
    void faceDownPileCanBeChosenForHand() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island, forest, mountain, plains));

        resolveScheme();
        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains(island.getName(), forest.getName())
                .doesNotContain(mountain.getName(), plains.getName());

        harness.handleMayAbilityChosen(player2, false);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(mountain, plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
    }

    @Test
    void multipleOpponentsStillRequireSeparationAndPileChoice() {
        Card island = new Island();
        Card forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island, forest));
        UUID thirdPlayerId = UUID.randomUUID();
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerIdToName.put(thirdPlayerId, "Third player");

        queueScheme();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void faceDownCardsPutIntoHandAreNotNamedInPublicLog() {
        Card island = new Island();
        Card forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island, forest));

        resolveScheme();
        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(forest.getName()));
    }

    @Test
    void singleFaceDownCardPutOnBottomIsNotNamedInPublicLog() {
        Card island = new Island();
        Card forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island, forest));

        resolveScheme();
        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(forest.getName()));
    }

    @Test
    void opponentCanChooseEmptyPileAndControllerOrdersAllCardsOnBottom() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card untouched = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island, forest, mountain, plains, untouched));

        resolveScheme();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, plains, mountain, forest, island);
    }

    @Test
    void emptyLibraryFinishesWithoutAChoice() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        resolveScheme();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void resolveScheme() {
        queueScheme();
        harness.passBothPriorities();
    }

    private void queueScheme() {
        ChooseYourDemise scheme = new ChooseYourDemise();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
    }
}
