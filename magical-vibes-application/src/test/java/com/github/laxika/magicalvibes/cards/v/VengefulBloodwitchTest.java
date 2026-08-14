package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VengefulBloodwitchTest extends BaseCardTest {

    @Test
    @DisplayName("When Vengeful Bloodwitch dies, target opponent loses 1 life and controller gains 1 life")
    void selfDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new VengefulBloodwitch());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, "Vengeful Bloodwitch");

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("When another creature you control dies, Vengeful Bloodwitch drains an opponent")
    void allyCreatureDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new VengefulBloodwitch());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, "Grizzly Bears");

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player controller, String targetName) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(controller, targetName);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }
}
