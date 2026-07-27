package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ManOWarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Man-o'-War enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castManOWar(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Man-o'-War");
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

    private void castManOWar(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new ManOWar()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }
}
