package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GladeWatcher.class, GrizzlyBears.class})
class GladeWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents attacking without activating the ability")
    void cannotAttackWithoutActivation() {
        addWatcherReady();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Formidable ability cannot be activated below total power eight")
    void cannotActivateBelowThreshold() {
        addWatcherReady();
        addGrizzlyBears(1);
        addGreenMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
    }

    @Test
    @DisplayName("Formidable ability lets Glade Watcher attack this turn")
    void activationAllowsAttacking() {
        Permanent watcher = addWatcherReady();
        addGrizzlyBears(3);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addGreenMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(watcher.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Attack permission wears off at end of turn")
    void attackPermissionWearsOffAtEndOfTurn() {
        addWatcherReady();
        addGrizzlyBears(3);
        addGreenMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addWatcherReady() {
        return addCreatureReady(player1, new GladeWatcher());
    }

    private void addGrizzlyBears(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
    }

    private void addGreenMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
