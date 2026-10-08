package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WildColos.class)
class WildColosTest extends BaseCardTest {

    @Test
    @DisplayName("Wild Colos can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        Permanent colos = harness.addToBattlefieldAndReturn(player1, new WildColos());
        colos.setSummoningSick(true);

        declareAttackers(List.of(0));
    }

    @Test
    @DisplayName("Haste does not allow a tapped Wild Colos to attack")
    void cannotAttackWhileTappedDespiteHaste() {
        Permanent colos = harness.addToBattlefieldAndReturn(player1, new WildColos());
        colos.setSummoningSick(true);
        colos.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
