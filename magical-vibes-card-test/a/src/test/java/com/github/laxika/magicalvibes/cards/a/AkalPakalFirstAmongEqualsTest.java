package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BuriedTreasure;
import com.github.laxika.magicalvibes.cards.r.RiverHeraldScout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkalPakalFirstAmongEquals.class, BuriedTreasure.class, RiverHeraldScout.class})
class AkalPakalFirstAmongEqualsTest extends BaseCardTest {

    @Test
    @DisplayName("At each end step, an artifact entered under the controller's control lets them choose one of the top two")
    void triggersOnEachEndStepAfterArtifactEntersUnderControllersControl() {
        Card chosen = new RiverHeraldScout();
        Card other = new RiverHeraldScout();
        harness.setLibrary(player1, List.of(chosen, other));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());

        castArtifact(player1);
        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    @DisplayName("Does not trigger when no artifact entered under the controller's control")
    void doesNotTriggerWithoutControlledArtifactEntry() {
        harness.setLibrary(player1, List.of(new RiverHeraldScout(), new RiverHeraldScout()));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger it")
    void doesNotTriggerForOpponentsArtifact() {
        harness.setLibrary(player1, List.of(new RiverHeraldScout(), new RiverHeraldScout()));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());

        castArtifact(player2);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mustPutOneCardIntoHandWhenTwoCardsAreAvailable() {
        Card first = new RiverHeraldScout();
        Card second = new RiverHeraldScout();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());
        castArtifact(player1);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    void putsOnlyRemainingLibraryCardIntoHand() {
        Card remaining = new RiverHeraldScout();
        harness.setLibrary(player1, List.of(remaining));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());
        castArtifact(player1);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoiceOrCauseADrawLoss() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());
        castArtifact(player1);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void countsArtifactThatEnteredBeforeAkalPakalAndWasSacrificed() {
        Card chosen = new RiverHeraldScout();
        Card other = new RiverHeraldScout();
        harness.setLibrary(player1, List.of(chosen, other));
        castArtifact(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    void multipleArtifactsProduceOnlyOneTriggerAndLeaveDeeperCardsUntouched() {
        Card first = new RiverHeraldScout();
        Card chosen = new RiverHeraldScout();
        Card deeper = new RiverHeraldScout();
        harness.setLibrary(player1, List.of(first, chosen, deeper));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());
        castArtifact(player1);
        castArtifact(player1);
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(deeper);
        assertThat(gd.stack).isEmpty();
    }

    private void castArtifact(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new BuriedTreasure(), "{2}");
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
