package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExclusionMageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Exclusion Mage enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMage(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Exclusion Mage");
    }

    @Test
    @DisplayName("ETB returns the targeted opponent creature to its owner's hand")
    void etbBouncesOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMage(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Exclusion Mage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new ExclusionMage()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearsId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMage(UUID targetId) {
        harness.setHand(player1, List.of(new ExclusionMage()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
