package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParapetWatchersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with white mana gives +0/+1")
    void activatingWithWhiteBoostsToughness() {
        Permanent watchers = addReadyWatchers(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchers.getPowerModifier()).isEqualTo(0);
        assertThat(watchers.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can also be paid with blue mana (hybrid)")
    void activatingWithBlueBoostsToughness() {
        Permanent watchers = addReadyWatchers(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchers.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +0/+1")
    void canActivateMultipleTimes() {
        Permanent watchers = addReadyWatchers(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(watchers.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent watchers = addReadyWatchers(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchers.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(watchers.getPowerModifier()).isEqualTo(0);
        assertThat(watchers.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyWatchers(Player player) {
        ParapetWatchers card = new ParapetWatchers();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
