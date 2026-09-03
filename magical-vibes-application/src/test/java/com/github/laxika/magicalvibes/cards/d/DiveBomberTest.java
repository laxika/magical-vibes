package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiveBomber.class, GrizzlyBears.class})
class DiveBomberTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to an attacking creature")
    void dealsDamageToAttackingCreature() {
        addCreatureReady(player1, new DiveBomber());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, 0, null, attacker.getId());

        harness.assertInGraveyard(player1, "Dive Bomber");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to a blocking creature")
    void dealsDamageToBlockingCreature() {
        addCreatureReady(player1, new DiveBomber());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());

        harness.assertInGraveyard(player1, "Dive Bomber");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addCreatureReady(player1, new DiveBomber());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }
}
