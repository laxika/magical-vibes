package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CascadeBluffs;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoggleBridgebreaker.class, CascadeBluffs.class})
class NoggleBridgebreakerTest extends BaseCardTest {

    private void castBridgebreaker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NoggleBridgebreaker()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("ETB trigger goes on the stack without choosing a land")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new CascadeBluffs());
        castBridgebreaker();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Noggle Bridgebreaker");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB resolves by choosing and returning a controlled land")
    void etbBouncesOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CascadeBluffs());
        castBridgebreaker();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, land.getId());

        harness.assertNotOnBattlefield(player1, "Cascade Bluffs");
        harness.assertInHand(player1, "Cascade Bluffs");
    }

    @Test
    @DisplayName("With no controlled lands the trigger still stacks and resolves without returning anything")
    void noControlledLandStillTriggers() {
        harness.addToBattlefield(player2, new CascadeBluffs());
        castBridgebreaker();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Cascade Bluffs");
        harness.assertOnBattlefield(player1, "Noggle Bridgebreaker");
    }

    @Test
    @DisplayName("The resolution-time choice includes only lands currently controlled")
    void choosesLandPresentAtResolution() {
        Permanent originalLand = harness.addToBattlefieldAndReturn(player1, new CascadeBluffs());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new CascadeBluffs());
        castBridgebreaker();
        harness.passBothPriorities();
        Permanent newLand = harness.addToBattlefieldAndReturn(player1, new CascadeBluffs());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrder(originalLand.getId(), newLand.getId())
                .doesNotContain(opposingLand.getId(), harness.getPermanentId(player1, "Noggle Bridgebreaker"));
        harness.handlePermanentChosen(player1, newLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(originalLand).doesNotContain(newLand);
        harness.assertInHand(player1, "Cascade Bluffs");
        harness.assertOnBattlefield(player2, "Cascade Bluffs");
    }

    @Test
    @DisplayName("A borrowed land is returned to its owner's hand")
    void returnsLandToOwner() {
        CascadeBluffs borrowedCard = new CascadeBluffs();
        borrowedCard.setOwnerId(player2.getId());
        Permanent borrowedLand = harness.addToBattlefieldAndReturn(player1, borrowedCard);
        castBridgebreaker();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, borrowedLand.getId());

        harness.assertNotOnBattlefield(player1, "Cascade Bluffs");
        harness.assertInHand(player2, "Cascade Bluffs");
        harness.assertNotInHand(player1, "Cascade Bluffs");
    }
}
