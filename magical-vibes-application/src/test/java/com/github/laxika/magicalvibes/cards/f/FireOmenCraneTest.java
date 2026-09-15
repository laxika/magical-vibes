package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireOmenCrane.class, GrizzlyBears.class})
class FireOmenCraneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 1 damage to target creature an opponent controls")
    void attackingDealsDamageToOpponentsCreature() {
        addCreatureReady(player1, new FireOmenCrane());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opposing.getId());
        harness.passBothPriorities();

        assertThat(opposing.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking cannot target a creature controlled by its controller")
    void attackingCannotTargetOwnCreature() {
        addCreatureReady(player1, new FireOmenCrane());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, opposing.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposing.getMarkedDamage()).isEqualTo(1);
    }
}
