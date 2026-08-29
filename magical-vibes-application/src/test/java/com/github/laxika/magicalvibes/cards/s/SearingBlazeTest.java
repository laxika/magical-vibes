package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearingBlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the targeted player and creature without landfall")
    void dealsOneDamageWithoutLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 3 damage to both targets after a land entered under the controller's control")
    void dealsThreeDamageWithLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new SearingBlaze()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows targeting a creature controlled by the targeted player")
    void targetsCreatureControlledByTargetedPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects a creature controlled by another player")
    void rejectsCreatureControlledByAnotherPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
