package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MoxSapphire.class)
class MoxSapphireTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mox Sapphire adds one blue mana")
    void tappingAddsBlueMana() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxSapphire());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Mox Sapphire cannot produce more mana")
    void cannotActivateWhileTapped() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxSapphire());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mox Sapphire can produce mana again after untapping")
    void canActivateAgainAfterUntapping() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxSapphire());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.performUntapStep(player1);
        assertThat(mox.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mox Sapphire can produce mana immediately after resolving")
    void canActivateImmediatelyAfterResolving() {
        harness.castFromHand(player1, new MoxSapphire(), "{0}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Mox Sapphire").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
