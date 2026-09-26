package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormOfSteel.class, GrizzlyBears.class})
class StormOfSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each of two targets")
    void dealsDamageToTwoTargets() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castStormOfSteel(List.of(firstBear.getId(), secondBear.getId()));

        assertThat(firstBear.getMarkedDamage()).isEqualTo(2);
        assertThat(secondBear.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can deal 2 damage to one target")
    void dealsDamageToOneTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castStormOfSteel(List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(untargeted.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires at least one target")
    void requiresAtLeastOneTarget() {
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castStormOfSteel(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new StormOfSteel()));
        addMana();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
