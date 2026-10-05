package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MoxJet.class)
class MoxJetTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mox Jet adds one black mana")
    void tappingAddsBlackMana() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxJet());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Mox Jet cannot produce mana again")
    void cannotActivateWhileTapped() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxJet());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mox Jet can produce mana again after untapping")
    void canActivateAfterUntapping() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxJet());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.performUntapStep(player1);
        assertThat(mox.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
