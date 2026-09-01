package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiteOfUndoingTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonland permanent controlled by each player to its owner's hand")
    void bouncesOnePermanentControlledByEachPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RiteOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingPermanentId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(ownPermanentId, opposingPermanentId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Rejects land and same-controller targets")
    void rejectsIllegalTargets() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RiteOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingPermanentId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forestId, opposingPermanentId)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownPermanentId, ownPermanentId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
