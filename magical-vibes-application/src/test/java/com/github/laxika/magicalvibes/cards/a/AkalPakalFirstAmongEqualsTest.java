package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkalPakalFirstAmongEquals.class, WornPowerstone.class, GrizzlyBears.class})
class AkalPakalFirstAmongEqualsTest extends BaseCardTest {

    @Test
    @DisplayName("At each end step, an artifact entered under the controller's control lets them choose one of the top two")
    void triggersOnEachEndStepAfterArtifactEntersUnderControllersControl() {
        Card chosen = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, other));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());

        castWornPowerstone(player1);
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
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger it")
    void doesNotTriggerForOpponentsArtifact() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new AkalPakalFirstAmongEquals());

        castWornPowerstone(player2);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWornPowerstone(Player player) {
        harness.setHand(player, List.of(new WornPowerstone()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castArtifact(player, 0);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
