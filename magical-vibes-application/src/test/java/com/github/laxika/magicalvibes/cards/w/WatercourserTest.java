package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WatercourserTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/-1")
    void activatingAbilityBoostsPowerAndLowersToughness() {
        Permanent courser = addReadyWatercourser(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(courser.getPowerModifier()).isEqualTo(1);
        assertThat(courser.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Activating three times kills it via state-based actions")
    void activatingThreeTimesKillsIt() {
        addReadyWatercourser(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Watercourser");
        harness.assertInGraveyard(player1, "Watercourser");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent courser = addReadyWatercourser(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(courser.getPowerModifier()).isEqualTo(2);
        assertThat(courser.getToughnessModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(courser.getPowerModifier()).isEqualTo(0);
        assertThat(courser.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyWatercourser(Player player) {
        Permanent perm = new Permanent(new Watercourser());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
