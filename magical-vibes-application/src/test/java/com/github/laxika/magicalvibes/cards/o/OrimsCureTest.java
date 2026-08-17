package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrimsCureTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 4 damage to a target player")
    void preventsNextFourDamageToPlayer() {
        harness.setHand(player1, List.of(new OrimsCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("May be cast for its alternate cost by tapping an untapped creature while controlling a Plains")
    void castsWithAlternateCost() {
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID creatureId = creature.getId();
        harness.setHand(player1, List.of(new OrimsCure()));

        harness.castInstantWithAlternateCost(player1, 0, player2.getId(), List.of(creatureId));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Alternate cost is unavailable without controlling a Plains")
    void alternateCostRequiresPlains() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrimsCure()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
