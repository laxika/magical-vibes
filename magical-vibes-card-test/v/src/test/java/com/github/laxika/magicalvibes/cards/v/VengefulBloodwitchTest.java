package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VengefulBloodwitch.class, GrizzlyBears.class, Shock.class, DayOfJudgment.class})
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

    @Test
    @DisplayName("An opposing creature dying does not trigger Vengeful Bloodwitch")
    void opposingCreatureDeathDoesNotDrain() {
        harness.addToBattlefield(player1, new VengefulBloodwitch());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player2, "Grizzly Bears");

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two Bloodwitches dying together each trigger for themselves and the other")
    void simultaneousDeathsTriggerForEachCreature() {
        harness.addToBattlefield(player1, new VengefulBloodwitch());
        harness.addToBattlefield(player1, new VengefulBloodwitch());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new DayOfJudgment(), "{2}{W}{W}");
        harness.passBothPriorities();

        for (int i = 0; i < 4; i++) {
            PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                    PendingInteraction.PermanentChoice.class);
            assertThat(choice.validIds()).containsExactly(player2.getId());
            harness.handlePermanentChosen(player1, player2.getId());
        }
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Vengeful Bloodwitch");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player controller, String targetName) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(controller, targetName);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
