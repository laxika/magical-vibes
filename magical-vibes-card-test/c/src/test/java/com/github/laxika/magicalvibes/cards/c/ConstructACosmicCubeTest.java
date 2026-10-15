package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConstructACosmicCube.class, Island.class})
class ConstructACosmicCubeTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a Villain and adds a plan counter")
    void secondDrawCreatesVillainAndAddsPlanCounter() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new ConstructACosmicCube());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));

        draw(player1);
        assertThat(gd.stack).isEmpty();

        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Villain")).hasSize(1);
        Permanent villain = findPermanents(player1, "Villain").get(0);
        assertThat(villain.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(villain.getCard().getSubtypes()).containsExactly(CardSubtype.VILLAIN);
        assertThat(villain.getCard().getKeywords()).contains(Keyword.MENACE);
        assertThat(gqs.getEffectivePower(gd, villain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, villain)).isEqualTo(1);
        assertThat(cube.getCounterCount(CounterType.PLAN)).isEqualTo(1);

        draw(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The seventh plan counter sacrifices the Cube and controls an opponent's next turn")
    void seventhPlanCounterSacrificesAndControlsOpponentNextTurn() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new ConstructACosmicCube());
        cube.setCounterCount(CounterType.PLAN, 6);

        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);
        resolveTopOfStack();
        assertThat(cube.getCounterCount(CounterType.PLAN)).isEqualTo(7);

        resolveTopOfStack();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validPlayerIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveTopOfStack();

        harness.assertNotOnBattlefield(player1, "Construct a Cosmic Cube");
        harness.assertInGraveyard(player1, "Construct a Cosmic Cube");
        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("Removing a plan counter after the seventh-counter trigger does not stop the sacrifice")
    void counterRemovedInResponseDoesNotStopSacrifice() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new ConstructACosmicCube());
        cube.setCounterCount(CounterType.PLAN, 6);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);
        resolveTopOfStack();
        assertThat(gd.stack).hasSize(1);

        cube.setCounterCount(CounterType.PLAN, 6);
        resolveTopOfStack();

        harness.assertInGraveyard(player1, "Construct a Cosmic Cube");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveTopOfStack();
        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger the Cube")
    void opponentSecondDrawDoesNotTrigger() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new ConstructACosmicCube());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Villain")).isEmpty();
        assertThat(cube.getCounterCount(CounterType.PLAN)).isZero();
    }

    @Test
    @DisplayName("The second draw triggers during an opponent's turn")
    void secondDrawOnOpponentTurnTriggers() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new ConstructACosmicCube());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        draw(player1);
        draw(player1);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Villain")).hasSize(1);
        assertThat(cube.getCounterCount(CounterType.PLAN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The draw trigger still creates a Villain when its source has left the battlefield")
    void sourceLeavingDoesNotStopTokenCreation() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new ConstructACosmicCube());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);
        gd.playerBattlefields.get(player1.getId()).remove(cube);
        gd.playerGraveyards.get(player1.getId()).add(cube.getCard());

        resolveTopOfStack();

        assertThat(findPermanents(player1, "Villain")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingTurnControl).isEmpty();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
