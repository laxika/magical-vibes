package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DarkProphecyTest extends BaseCardTest {

    // "Whenever a creature you control dies, you draw a card and you lose 1 life."

    /** Player1 shocks a creature; resolve Shock, the death, then the death triggers. */
    private void killWithShock(com.github.laxika.magicalvibes.model.Player owner, String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(owner, targetName);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve Shock -> creature dies -> triggers onto stack
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger
    }

    @Test
    @DisplayName("Your creature dying draws a card and costs 1 life")
    void ownCreatureDeathDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1); // Shock was cast, one card drawn
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player2, new GrizzlyBears());

        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(player2, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
