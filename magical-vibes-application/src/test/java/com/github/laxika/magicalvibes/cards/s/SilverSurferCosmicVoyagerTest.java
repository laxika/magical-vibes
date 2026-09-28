package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverSurferCosmicVoyager.class, GrizzlyBears.class, Forest.class})
class SilverSurferCosmicVoyagerTest extends BaseCardTest {

    @Test
    void exilesAnyNumberOfOtherPermanentsAndReturnsLandsTappedAtNextEndStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SilverSurferCosmicVoyager()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(bears.getId(), forest.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentBears.getId(),
                harness.getPermanentId(player1, "Silver Surfer, Cosmic Voyager"));

        harness.handlePermanentChosen(player1, bears.getId());
        choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(forest.getId());
        assertThat(choice.validPlayerIds()).contains(player1.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        advanceToEndStep();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        Permanent returnedForest = findPermanent(player1, "Forest");
        assertThat(returnedBears.isTapped()).isFalse();
        assertThat(returnedForest.isTapped()).isTrue();
    }

    @Test
    void mayChooseNoPermanents() {
        harness.setHand(player1, List.of(new SilverSurferCosmicVoyager()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
