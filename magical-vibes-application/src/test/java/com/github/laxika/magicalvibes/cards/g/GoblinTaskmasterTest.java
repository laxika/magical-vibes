package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTaskmaster.class, GoblinWardriver.class, GrizzlyBears.class})
class GoblinTaskmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives a target Goblin +1/+0 until end of turn")
    void boostsTargetGoblin() {
        addTaskmaster();
        Permanent goblin = addGoblin(player1);
        int originalPower = goblin.getEffectivePower();
        int originalToughness = goblin.getEffectiveToughness();

        activateTaskmaster(goblin.getId());

        assertThat(goblin.getEffectivePower()).isEqualTo(originalPower + 1);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("Ability can target an opponent's Goblin")
    void boostsOpponentsGoblin() {
        addTaskmaster();
        Permanent goblin = addGoblin(player2);
        int originalPower = goblin.getEffectivePower();

        activateTaskmaster(goblin.getId());

        assertThat(goblin.getEffectivePower()).isEqualTo(originalPower + 1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addTaskmaster();
        Permanent goblin = addGoblin(player1);
        int originalPower = goblin.getEffectivePower();

        activateTaskmaster(goblin.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(goblin.getEffectivePower()).isEqualTo(originalPower);
    }

    @Test
    @DisplayName("Ability cannot target a non-Goblin creature")
    void rejectsNonGoblinTarget() {
        addTaskmaster();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addTaskmaster() {
        harness.addToBattlefield(player1, new GoblinTaskmaster());
        findPermanent(player1, "Goblin Taskmaster").setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private Permanent addGoblin(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GoblinWardriver());
    }

    private void activateTaskmaster(UUID targetId) {
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }
}
