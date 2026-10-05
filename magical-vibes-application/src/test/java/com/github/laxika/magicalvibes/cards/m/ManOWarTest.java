package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManOWar.class, GrizzlyBears.class, Island.class})
class ManOWarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Man-o'-War enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castManOWar(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB resolves: target creature is returned to owner's hand")
    void etbBouncesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castManOWar(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Man-o'-War enters the battlefield after resolution")
    void manOWarEntersBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castManOWar(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Man-o'-War");
    }

    @Test
    @DisplayName("Can bounce own creature")
    void canBounceOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castManOWar(player1, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner's hand")
    void returnsControlledCreatureToOwnersHand() {
        GrizzlyBears target = new GrizzlyBears();
        target.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, target);
        castManOWar(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does nothing if the target creature leaves before resolution")
    void etbDoesNothingIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castManOWar(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertOnBattlefield(player1, "Man-o'-War");
    }

    @Test
    @DisplayName("Must return itself when it is the only creature")
    void returnsItselfWhenOnlyCreature() {
        harness.castFromHand(player1, new ManOWar(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Man-o'-War"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Man-o'-War");
        harness.assertInHand(player1, "Man-o'-War");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can choose itself even when another creature is present")
    void canChooseItselfWithOtherCreaturePresent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new ManOWar(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Man-o'-War"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Man-o'-War");
        harness.assertNotOnBattlefield(player1, "Man-o'-War");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Chooses a legal ETB target after a suggested target leaves before entry")
    void choosesLegalTargetAfterSuggestedTargetLeavesBeforeEntry() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castManOWar(player2, "Grizzly Bears");
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Man-o'-War");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Man-o'-War"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Man-o'-War");
        harness.assertInHand(player1, "Man-o'-War");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ManOWar()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castManOWar(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new ManOWar()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetId);
    }
}
