package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DwarvenSeaClanTest extends BaseCardTest {

    @Test
    @DisplayName("Damage is dealt only at end of combat, killing a 2/2 attacker")
    void dealsTwoDamageAtEndOfCombat() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);

        harness.activateAbility(player1, indexOf(player1, clan), 0, null, attacker.getId());
        harness.passBothPriorities();

        // Still alive — the damage is delayed until combat ends.
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature that survives the 2 damage stays on the battlefield with damage marked")
    void survivorKeepsMarkedDamage() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new com.github.laxika.magicalvibes.cards.h.HillGiant());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);

        harness.activateAbility(player1, indexOf(player1, clan), 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an attacking creature whose controller controls no Island")
    void cannotTargetWithoutIsland() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);

        int index = indexOf(player1, clan);
        UUID targetId = attacker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatant() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        int index = indexOf(player1, clan);
        UUID targetId = bystander.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated during the end of combat step")
    void cannotActivateDuringEndOfCombat() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        attacker.setAttacking(true);

        int index = indexOf(player1, clan);
        UUID targetId = attacker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before the end of combat step");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
