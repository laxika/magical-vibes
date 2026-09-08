package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StampedingWildebeests.class, GrizzlyBears.class, RagingGoblin.class})
class StampedingWildebeestsTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers only during its controller upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        addCreatureReady(player1, new StampedingWildebeests());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("Stampeding Wildebeests's upkeep ability");
    }

    @Test
    @DisplayName("Prompt only includes green creatures you control")
    void promptOnlyIncludesGreenCreaturesYouControl() {
        addCreatureReady(player1, new StampedingWildebeests());
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonGreenCreature = addCreatureReady(player1, new RagingGoblin());
        Permanent opponentsGreenCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(greenCreature.getId())
                .doesNotContain(nonGreenCreature.getId())
                .doesNotContain(opponentsGreenCreature.getId());
    }

    @Test
    @DisplayName("Can choose itself when it is the only green creature")
    void canChooseItselfWhenOnlyGreenCreature() {
        Permanent wildebeests = addCreatureReady(player1, new StampedingWildebeests());
        addCreatureReady(player1, new RagingGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).containsExactly(wildebeests.getId());
    }

    @Test
    @DisplayName("Chosen green creature is returned to owner's hand")
    void chosenGreenCreatureReturnedToOwnersHand() {
        addCreatureReady(player1, new StampedingWildebeests());
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, greenCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(greenCreature.getId()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a controlled green creature to its owner's hand")
    void returnsControlledGreenCreatureToItsOwnersHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new StampedingWildebeests());

        var opponentOwnedCard = new GrizzlyBears();
        opponentOwnedCard.setOwnerId(player2.getId());
        Permanent opponentOwnedCreature = addCreatureReady(player1, opponentOwnedCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentOwnedCreature.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent wildebeests = addCreatureReady(player1, new StampedingWildebeests());
        wildebeests.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wildebeests);
    }
}
