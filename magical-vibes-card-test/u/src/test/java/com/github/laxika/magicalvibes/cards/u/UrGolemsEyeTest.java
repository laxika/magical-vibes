package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrGolemsEye.class})
class UrGolemsEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds two colorless mana")
    void tapForTwoColorlessMana() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new UrGolemsEye());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(eye.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for mana adds two colorless mana to the existing pool")
    void addsToExistingManaPool() {
        harness.addToBattlefield(player1, new UrGolemsEye());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate the mana ability while Ur-Golem's Eye is tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new UrGolemsEye());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The mana ability can be activated again after untapping")
    void canActivateAgainAfterUntapping() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new UrGolemsEye());
        harness.activateAbility(player1, 0, null, null);

        harness.performUntapStep(player1);
        assertThat(eye.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);

        assertThat(eye.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mana is added to the activating controller's pool")
    void addsManaToOtherControllersPool() {
        Permanent eye = harness.addToBattlefieldAndReturn(player2, new UrGolemsEye());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(eye.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
