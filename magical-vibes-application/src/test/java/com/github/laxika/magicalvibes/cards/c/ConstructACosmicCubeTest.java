package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ConstructACosmicCube.class, GrizzlyBears.class})
class ConstructACosmicCubeTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a Villain and adds a plan counter")
    void secondDrawCreatesVillainAndAddsPlanCounter() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new ConstructACosmicCube());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

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

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
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

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
