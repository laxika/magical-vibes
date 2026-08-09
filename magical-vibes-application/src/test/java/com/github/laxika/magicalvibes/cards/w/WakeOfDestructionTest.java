package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WakeOfDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target land and every other land with the same name")
    void destroysTargetAndAllSameNameLands() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Plains());

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
