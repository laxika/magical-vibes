package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ThunderingGiant.class)
class ThunderingGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield due to haste")
    void canAttackWithSummoningSicknessDueToHaste() {
        harness.castFromHand(player1, new ThunderingGiant(), "{3}{R}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent giant = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(giant.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Haste does not allow a tapped creature to attack")
    void cannotAttackWhileTappedDespiteHaste() {
        harness.castFromHand(player1, new ThunderingGiant(), "{3}{R}{R}");
        harness.passBothPriorities();
        Permanent giant = findPermanent(player1, "Thundering Giant");
        giant.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}


