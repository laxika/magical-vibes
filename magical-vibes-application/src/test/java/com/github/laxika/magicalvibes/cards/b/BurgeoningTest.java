package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurgeoningTest extends BaseCardTest {

    @Test
    @DisplayName("May put a land from hand onto the battlefield when an opponent plays a land")
    void putsLandOntoBattlefieldAfterOpponentLandPlay() {
        harness.addToBattlefield(player1, new Burgeoning());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));

        prepareOpponentLandPlay();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the trigger leaves the land in hand")
    void decliningTriggerLeavesLandInHand() {
        harness.addToBattlefield(player1, new Burgeoning());
        harness.setHand(player1, List.of(new Forest()));

        prepareOpponentLandPlay();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Does not trigger when its controller plays a land")
    void doesNotTriggerForControllerLandPlay() {
        harness.addToBattlefield(player1, new Burgeoning());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters without being played")
    void doesNotTriggerForLandPutOntoBattlefield() {
        harness.addToBattlefield(player1, new Burgeoning());

        harness.addToBattlefield(player2, new Forest());

        assertThat(gd.stack).isEmpty();
    }

    private void prepareOpponentLandPlay() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
