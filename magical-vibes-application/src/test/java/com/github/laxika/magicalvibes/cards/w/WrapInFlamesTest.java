package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WrapInFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to and stops up to three target creatures from blocking")
    void damagesAndStopsThreeCreaturesFromBlocking() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature3 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(creature1.getId(), creature2.getId(), creature3.getId()));
        harness.passBothPriorities();

        assertThat(creature1.getMarkedDamage()).isEqualTo(1);
        assertThat(creature2.getMarkedDamage()).isEqualTo(1);
        assertThat(creature3.getMarkedDamage()).isEqualTo(1);
        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
        assertThat(creature3.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target fewer than three creatures")
    void canTargetFewerCreatures() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(creature1.getMarkedDamage()).isEqualTo(1);
        assertThat(creature2.getMarkedDamage()).isEqualTo(1);
        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
