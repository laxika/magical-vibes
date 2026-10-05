package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistRaven.class, GrizzlyBears.class})
class MistRavenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Mist Raven enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMistRaven(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mist Raven");
    }

    @Test
    @DisplayName("ETB resolves: target creature is returned to owner's hand")
    void etbBouncesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMistRaven(player2, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mist Raven enters the battlefield after resolution")
    void mistRavenEntersBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMistRaven(player2, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mist Raven");
    }

    @Test
    @DisplayName("Can bounce own creature")
    void canBounceOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castMistRaven(player1, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Must return itself when it is the only creature")
    void returnsItselfWhenOnlyCreature() {
        harness.castFromHand(player1, new MistRaven(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Mist Raven"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mist Raven");
        harness.assertInHand(player1, "Mist Raven");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trigger still returns the target after Mist Raven leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMistRaven(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToHand(gd, findPermanent(player1, "Mist Raven"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Mist Raven");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves before resolution is not returned again")
    void targetLeavesBeforeResolution() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMistRaven(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToHand(gd, findPermanent(player2, "Grizzly Bears"));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mist Raven");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns a creature to its owner rather than its controller")
    void returnsCreatureToOwner() {
        harness.setHand(player2, List.of());
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, bears);
        castMistRaven(player2, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void castMistRaven(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new MistRaven()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }
}
