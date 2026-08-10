package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PewterGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Pewter Golem's ability grants a regeneration shield and pays {1}{B}")
    void activatingAbilityGrantsRegenerationShield() {
        Permanent golem = addReadyGolem(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Pewter Golem's regeneration shield clears during cleanup")
    void regenerationShieldClearsAtEndOfTurn() {
        Permanent golem = addReadyGolem(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(golem.getRegenerationShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isZero();
    }

    private Permanent addReadyGolem(Player player) {
        Permanent golem = new Permanent(new PewterGolem());
        golem.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(golem);
        return golem;
    }
}
