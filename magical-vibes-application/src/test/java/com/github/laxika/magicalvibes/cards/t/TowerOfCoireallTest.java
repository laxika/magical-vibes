package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CarnivorousPlant;
import com.github.laxika.magicalvibes.cards.l.LandLeeches;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowerOfCoireall.class, LandLeeches.class, CarnivorousPlant.class})
class TowerOfCoireallTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature cannot be blocked by a Wall")
    void wallCannotBlock() {
        Permanent attacker = activateTowerAndSetAttacker();
        Permanent wall = addCreatureReady(player2, new CarnivorousPlant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(wall, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Wall creatures");
    }

    @Test
    @DisplayName("Target creature can be blocked by a non-Wall creature")
    void nonWallCanBlock() {
        Permanent attacker = activateTowerAndSetAttacker();
        Permanent blocker = addCreatureReady(player2, new LandLeeches());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Restriction wears off at end of turn")
    void restrictionWearsOff() {
        Permanent attacker = activateTowerAndSetAttacker();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent wall = addCreatureReady(player2, new CarnivorousPlant());
        prepareDeclareBlockers();
        declareBlock(wall, attacker);

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Target creature controlled by an opponent cannot be blocked by a Wall")
    void opponentCreatureCanBeTargeted() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfCoireall());
        tower.setSummoningSick(false);
        Permanent attacker = addCreatureReady(player2, new LandLeeches());
        Permanent wall = addCreatureReady(player1, new CarnivorousPlant());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(wall),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Wall creatures");
    }

    @Test
    @DisplayName("Walls can still block creatures that were not targeted")
    void untargetedCreatureCanBeBlockedByWall() {
        activateTowerAndSetAttacker();
        Permanent otherAttacker = addCreatureReady(player1, new LandLeeches());
        otherAttacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new CarnivorousPlant());

        prepareDeclareBlockers();
        declareBlock(wall, otherAttacker);

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A noncreature cannot be targeted and an illegal activation does not tap the Tower")
    void noncreatureCannotBeTargeted() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfCoireall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tower.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tower.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even if the Tower leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfCoireall());
        Permanent attacker = addCreatureReady(player1, new LandLeeches());
        Permanent wall = addCreatureReady(player2, new CarnivorousPlant());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(tower);
            gd.playerGraveyards.get(player1.getId()).add(tower.getCard());
        });
        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(wall, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Wall creatures");
    }

    private Permanent activateTowerAndSetAttacker() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfCoireall());
        tower.setSummoningSick(false);
        Permanent attacker = addCreatureReady(player1, new LandLeeches());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(tower.isTapped()).isTrue();
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
