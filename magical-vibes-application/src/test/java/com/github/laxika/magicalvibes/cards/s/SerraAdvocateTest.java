package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerraAdvocateTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an attacking creature +2/+2 until end of turn")
    void boostsAttackingCreature() {
        Permanent attacker = addSerraAdvocateAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts a blocking creature +2/+2 until end of turn")
    void boostsBlockingCreature() {
        Permanent blocker = addSerraAdvocateAndCombatCreature(false, true, player2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addSerraAdvocate();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps Serra Advocate when the ability is activated")
    void tapsOnActivation() {
        Permanent attacker = addSerraAdvocateAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(findPermanent(player1, "Serra Advocate").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent attacker = addSerraAdvocateAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    private void addSerraAdvocate() {
        harness.addToBattlefield(player1, new SerraAdvocate());
        findPermanent(player1, "Serra Advocate").setSummoningSick(false);
    }

    private Permanent addSerraAdvocateAndCombatCreature(boolean attacking, boolean blocking, Player controller) {
        addSerraAdvocate();
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        harness.getGameData().playerBattlefields.get(controller.getId()).add(creature);
        harness.forceActivePlayer(player1);
        harness.forceStep(attacking ? TurnStep.DECLARE_ATTACKERS : TurnStep.DECLARE_BLOCKERS);
        return creature;
    }
}
