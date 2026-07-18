package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprenticeWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Activating pays {U}, taps, and adds {C}{C}{C}")
    void activateAddsThreeColorless() {
        harness.addToBattlefield(player1, new ApprenticeWizard());
        GameData gd = harness.getGameData();
        Permanent wizard = gd.playerBattlefields.get(player1.getId()).getFirst();
        wizard.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(wizard.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty(); // mana ability resolves immediately
    }

    @Test
    @DisplayName("Cannot activate without {U} in the pool")
    void cannotActivateWithoutBlue() {
        harness.addToBattlefield(player1, new ApprenticeWizard());
        GameData gd = harness.getGameData();
        Permanent wizard = gd.playerBattlefields.get(player1.getId()).getFirst();
        wizard.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ApprenticeWizard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
