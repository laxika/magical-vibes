package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SaheelisLattice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CouncilOfEchoes.class, Colossadactyl.class, Island.class, Abrade.class, SaheelisLattice.class})
class CouncilOfEchoesTest extends BaseCardTest {

    @Test
    @DisplayName("Descend 4 returns up to one other nonland permanent")
    void descendFourReturnsTarget() {
        harness.addToBattlefield(player2, new Colossadactyl());
        harness.setGraveyard(player1, List.of(
                new Colossadactyl(), new Island(), new Colossadactyl(), new Island()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        UUID targetId = harness.getPermanentId(player2, "Colossadactyl");

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(targetId, player1.getId());
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Colossadactyl");
        harness.assertOnBattlefield(player1, "Council of Echoes");
    }

    @Test
    @DisplayName("Descend 4 does not count nonpermanent cards")
    void nonpermanentCardsDoNotEnableDescend() {
        harness.addToBattlefield(player2, new Colossadactyl());
        harness.setGraveyard(player1, List.of(
                new Colossadactyl(), new Island(), new Colossadactyl(), new Abrade()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Colossadactyl");
    }

    @Test
    @DisplayName("Descend 4 can choose no target even when one is available")
    void canChooseNoTarget() {
        harness.addToBattlefield(player2, new Colossadactyl());
        harness.setGraveyard(player1, List.of(
                new Colossadactyl(), new Island(), new Colossadactyl(), new Island()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Colossadactyl");
    }

    @Test
    @DisplayName("Descend 4 can return a noncreature artifact controlled by its controller")
    void canReturnOwnArtifact() {
        harness.addToBattlefield(player1, new SaheelisLattice());
        harness.setGraveyard(player1, List.of(
                new Colossadactyl(), new Island(), new SaheelisLattice(), new Island()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        UUID targetId = harness.getPermanentId(player1, "Saheeli's Lattice");

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, targetId);
        resolveAllTriggers();

        harness.assertInHand(player1, "Saheeli's Lattice");
        harness.assertOnBattlefield(player1, "Council of Echoes");
    }

    @Test
    @DisplayName("Descend 4 rechecks the graveyard when its trigger resolves")
    void graveyardThresholdIsRecheckedOnResolution() {
        harness.addToBattlefield(player2, new Colossadactyl());
        harness.setGraveyard(player1, List.of(
                new Colossadactyl(), new Island(), new Colossadactyl(), new Island()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        UUID targetId = harness.getPermanentId(player2, "Colossadactyl");

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, targetId);
        harness.setGraveyard(player1, List.of(new Colossadactyl(), new Island(), new Island()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Colossadactyl");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Permanent cards in an opponent's graveyard do not enable Descend 4")
    void opponentsGraveyardDoesNotEnableDescend() {
        harness.addToBattlefield(player2, new Colossadactyl());
        harness.setGraveyard(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player2, List.of(
                new Colossadactyl(), new Island(), new Colossadactyl(), new Island()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Colossadactyl");
    }

    @Test
    @DisplayName("The source and lands cannot be chosen")
    void sourceAndLandsAreNotLegalTargets() {
        harness.addToBattlefield(player1, new Island());
        harness.setGraveyard(player1, List.of(
                new Colossadactyl(), new Island(), new Colossadactyl(), new Island()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Council of Echoes");
        harness.assertOnBattlefield(player1, "Island");
    }
}
