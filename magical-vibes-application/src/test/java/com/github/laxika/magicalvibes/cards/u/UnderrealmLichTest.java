package com.github.laxika.magicalvibes.cards.u;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({UnderrealmLich.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class UnderrealmLichTest extends BaseCardTest {

    private void drawWithLich() {
        harness.addToBattlefield(player1, new UnderrealmLich());
        harness.setHand(player1, new ArrayList<>());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    @Test
    @DisplayName("A replaced draw puts the chosen card into hand and the rest into the graveyard")
    void chosenCardGoesToHandAndRestToGraveyard() {
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, new ArrayList<>(List.of(plains, bears, forest, island)));

        drawWithLich();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);
        assertThat(choice.remainingToGraveyard()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(plains, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("With fewer than three cards, all available cards are used")
    void usesAvailableCardsOnly() {
        Card bears = new GrizzlyBears();
        Card plains = new Plains();
        harness.setLibrary(player1, new ArrayList<>(List.of(bears, plains)));

        drawWithLich();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(plains);
    }

    @Test
    @DisplayName("An empty library does not cause a loss when the draw is replaced")
    void emptyLibraryDoesNotLose() {
        harness.setLibrary(player1, new ArrayList<>());

        drawWithLich();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The activated ability pays life, taps the Lich, and grants indestructible")
    void abilityPaysLifeTapsAndGrantsIndestructible() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new UnderrealmLich());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        harness.passBothPriorities();

        assertThat(lich.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void oneRemainingCardGoesToHandWithoutBeingDrawn() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);

        drawWithLich();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isZero();
    }

    @Test
    void multipleDrawsAreReplacedOneAtATime() {
        Card first = new Forest();
        Card second = new Plains();
        Card third = new Island();
        Card fourth = new GrizzlyBears();
        Card fifth = new Forest();
        Card sixth = new Plains();
        harness.addToBattlefield(player1, new UnderrealmLich());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, fifth, sixth);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)
                .allCards()).containsExactly(fourth, fifth, sixth);

        harness.handleMultipleCardsChosen(player1, List.of(fourth.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, third, fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isZero();
    }

    @Test
    void doesNotReplaceOpponentsDraw() {
        Card forest = new Forest();
        Card plains = new Plains();
        harness.addToBattlefield(player1, new UnderrealmLich());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(forest, plains));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(plains);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canActivateWhileTappedAndIndestructibleExpiresAtEndOfTurn() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new UnderrealmLich());
        lich.tap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(lich.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, lich, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @CardUsed({DeepFreeze.class})
    void losingAbilitiesStopsTheDrawReplacement() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new UnderrealmLich());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, lich.getId());
        harness.passBothPriorities();
        Card forest = new Forest();
        Card plains = new Plains();
        Card island = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest, plains, island));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, island);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
