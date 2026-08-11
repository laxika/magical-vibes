package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StandDeliverTest extends BaseCardTest {

    @Test
    @DisplayName("Stand prevents the next 2 damage to target creature")
    void standPreventsDamageToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new StandDeliver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deliver returns target permanent to its owner's hand")
    void deliverReturnsPermanentToHand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new StandDeliver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 1, island.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Stand cannot target a noncreature permanent")
    void standCannotTargetNoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new StandDeliver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID islandId = island.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, islandId))
                .isInstanceOf(IllegalStateException.class);
    }
}
