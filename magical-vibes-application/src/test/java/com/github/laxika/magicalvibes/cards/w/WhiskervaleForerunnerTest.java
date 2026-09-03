package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiskervaleForerunner.class, GiantGrowth.class, GrizzlyBears.class, Forest.class})
class WhiskervaleForerunnerTest extends BaseCardTest {

    @Test
    @DisplayName("On your turn, the chosen creature is offered for the battlefield")
    void putsChosenCreatureOntoBattlefieldOnYourTurn() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);
        castGrowth(player1, forerunner);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(bears);
    }

    @Test
    @DisplayName("Declining the battlefield placement puts the revealed creature into your hand")
    void declinesBattlefieldPlacementToHand() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);
        castGrowth(player1, forerunner);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("On another player's turn, the revealed creature goes directly to your hand")
    void putsCreatureIntoHandOnAnotherPlayersTurn() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, forerunner.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The valiant ability triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);

        castGrowth(player1, forerunner);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, forerunner.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    private Permanent addForerunner() {
        return addCreatureReady(player1, new WhiskervaleForerunner());
    }

    private void setTopFive(GrizzlyBears bears) {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), bears));
    }

    private void castGrowth(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
    }
}
