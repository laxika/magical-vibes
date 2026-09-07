package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeerlessRopemaster.class, GrizzlyBears.class})
class PeerlessRopemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a tapped creature to its owner's hand")
    void returnsTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        castPeerlessRopemaster(List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("May choose no target")
    void mayChooseNoTarget() {
        castPeerlessRopemaster(List.of());

        harness.assertOnBattlefield(player1, "Peerless Ropemaster");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PeerlessRopemaster()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    private void castPeerlessRopemaster(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new PeerlessRopemaster()));
        addMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
