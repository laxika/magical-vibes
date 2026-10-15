package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.ThrivingTurtle;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AviaryMechanic.class, Island.class, ThrivingTurtle.class})
class AviaryMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may prompt to return another permanent you control")
    void etbPromptsOptionalReturn() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        castAndResolveSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        UUID mechanicId = harness.getPermanentId(player1, "Aviary Mechanic");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(mechanicId);
    }

    @Test
    @DisplayName("Accepting the may ability returns the chosen permanent to its owner's hand")
    void acceptingReturnsChosenPermanent() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, islandId);

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Aviary Mechanic");
    }

    @Test
    @DisplayName("Declining the may ability leaves permanents on the battlefield")
    void decliningLeavesPermanentsOnBattlefield() {
        harness.addToBattlefield(player1, new Island());

        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Aviary Mechanic");
    }

    @Test
    @DisplayName("Opponent permanents are not valid choices")
    void opponentPermanentsAreNotChoices() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new ThrivingTurtle());
        UUID islandId = harness.getPermanentId(player1, "Island");
        UUID turtleId = harness.getPermanentId(player2, "Thriving Turtle");

        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId)
                .doesNotContain(turtleId);
    }

    @Test
    @DisplayName("Accepting with no other permanent completes without returning the mechanic")
    void noOtherPermanentCompletesWithoutReturn() {
        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Aviary Mechanic");
        harness.assertNotInHand(player1, "Aviary Mechanic");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another copy of Aviary Mechanic can be returned")
    void returnsAnotherCopy() {
        UUID otherId = harness.addToBattlefieldAndReturn(player1, new AviaryMechanic()).getId();

        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(otherId);
        harness.handlePermanentChosen(player1, otherId);

        harness.assertInHand(player1, "Aviary Mechanic");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getId()).isNotEqualTo(otherId);
    }

    @Test
    @DisplayName("A controlled permanent owned by the opponent returns to the opponent's hand")
    void returnsControlledPermanentToItsOwner() {
        ThrivingTurtle turtle = new ThrivingTurtle();
        turtle.setOwnerId(player2.getId());
        UUID turtleId = harness.addToBattlefieldAndReturn(player1, turtle).getId();
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.Forest());

        castAndResolveSpell();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, turtleId);

        harness.assertNotOnBattlefield(player1, "Thriving Turtle");
        harness.assertInHand(player2, "Thriving Turtle");
        harness.assertNotInHand(player1, "Thriving Turtle");
        harness.assertOnBattlefield(player1, "Aviary Mechanic");
    }

    private void castAndResolveSpell() {
        harness.castFromHand(player1, new AviaryMechanic(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
