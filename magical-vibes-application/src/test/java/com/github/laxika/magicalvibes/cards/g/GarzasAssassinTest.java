package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GarzasAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and destroys a target nonblack creature")
    void sacrificesAndDestroysNonblackCreature() {
        addReadyAssassin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Garza's Assassin");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        addReadyAssassin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");

        harness.assertOnBattlefield(player1, "Garza's Assassin");
    }

    @Test
    @DisplayName("Recover returns it to hand after paying half life")
    void recoverReturnsItToHandWhenPaid() {
        Card assassin = new GarzasAssassin();
        harness.setGraveyard(player1, List.of(assassin));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).contains(assassin);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(assassin);
    }

    @Test
    @DisplayName("Recover exiles it when declined")
    void recoverExilesItWhenDeclined() {
        Card assassin = new GarzasAssassin();
        harness.setGraveyard(player1, List.of(assassin));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(assassin);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(assassin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(assassin);
    }

    @Test
    @DisplayName("Recover does not trigger when Garza's Assassin itself dies")
    void recoverDoesNotTriggerForItsOwnDeath() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new GarzasAssassin());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, assassin));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Garza's Assassin");
    }

    private void addReadyAssassin() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new GarzasAssassin());
        assassin.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
