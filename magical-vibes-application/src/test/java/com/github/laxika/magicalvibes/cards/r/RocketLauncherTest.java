package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RocketLauncher.class)
class RocketLauncherTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to any target and destroys itself at the next end step")
    void damagesPlayerAndDestroysItselfAtEndStep() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());
        launcher.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(launcher);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(launcher);
        harness.assertInGraveyard(player1, "Rocket Launcher");
    }

    @Test
    @DisplayName("Cannot activate before controlling it continuously since the most recent turn began")
    void cannotActivateWhenItIsSummoningSick() {
        harness.addToBattlefield(player1, new RocketLauncher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("continuously");
    }
}
