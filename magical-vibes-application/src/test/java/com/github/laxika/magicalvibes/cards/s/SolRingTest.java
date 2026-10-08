package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SolRing.class)
class SolRingTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Sol Ring produces two colorless mana")
    void tappingProducesTwoColorlessMana() {
        Permanent solRing = harness.addToBattlefieldAndReturn(player1, new SolRing());
        solRing.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(solRing.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sol Ring can produce mana immediately after entering the battlefield")
    void canActivateImmediatelyAfterEntering() {
        Permanent solRing = harness.enterBattlefieldAndReturn(player1, new SolRing());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(solRing.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Sol Ring cannot produce more mana")
    void cannotActivateWhileTapped() {
        Permanent solRing = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(solRing.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sol Ring adds mana to its controller's pool without using the stack")
    void addsManaOnlyToItsController() {
        Permanent solRing = harness.addToBattlefieldAndReturn(player2, new SolRing());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(solRing.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
