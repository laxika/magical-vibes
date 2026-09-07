package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderWall.class, KjeldoranSkyknight.class})
class ThunderWallTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Thunder Wall from attacking")
    void defenderPreventsAttacking() {
        Permanent wall = addCreatureReady(player1, new ThunderWall());

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Flying lets Thunder Wall block a flying creature")
    void flyingLetsItBlockFlyingCreature() {
        addCreatureReady(player1, new KjeldoranSkyknight());
        Permanent wall = addCreatureReady(player2, new ThunderWall());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("{U}: gives +1/+1 until end of turn")
    void abilityBoosts() {
        Permanent wall = addCreatureReady(player1, new ThunderWall());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(1);
        assertThat(wall.getToughnessModifier()).isEqualTo(1);
        assertThat(wall.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The non-tap ability can be activated with summoning sickness")
    void abilityCanBeActivatedWithSummoningSickness() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new ThunderWall());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(1);
        assertThat(wall.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability multiple times stacks")
    void abilityStacks() {
        Permanent wall = addCreatureReady(player1, new ThunderWall());

        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(3);
        assertThat(wall.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent wall = addCreatureReady(player1, new ThunderWall());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(wall.getPowerModifier()).isEqualTo(1);
        assertThat(wall.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(0);
        assertThat(wall.getToughnessModifier()).isEqualTo(0);
    }
}
