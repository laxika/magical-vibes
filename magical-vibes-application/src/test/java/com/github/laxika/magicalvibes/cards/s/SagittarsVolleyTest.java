package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SagittarsVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target flier and damages opposing fliers")
    void destroysTargetAndDamagesOpposingFliers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent otherOpponentFlier = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Permanent ownFlier = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        Permanent opponentGroundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SagittarsVolley()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(otherOpponentFlier.getMarkedDamage()).isEqualTo(1);
        assertThat(ownFlier.getMarkedDamage()).isZero();
        assertThat(opponentGroundCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SagittarsVolley()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, groundCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }
}
