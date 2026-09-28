package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AkkiRaider;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinamoSightbender.class, AkkiRaider.class, GnarledMass.class, GodsEyeGateToTheReikai.class})
class MinamoSightbenderTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 makes a power-2 creature unblockable")
    void makesCreatureWithPowerAtMostXUnblockable() {
        Permanent source = addCreatureReady(player1, new MinamoSightbender());
        Permanent raider = addCreatureReady(player1, new AkkiRaider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, raider.getId());
        harness.passBothPriorities();

        assertThat(raider.isCantBeBlocked()).isTrue();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A creature with power greater than the paid X is an illegal target")
    void rejectsCreatureWithPowerAboveX() {
        Permanent source = addCreatureReady(player1, new MinamoSightbender());
        Permanent giant = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("A noncreature permanent is an illegal target")
    void rejectsNonCreatureTarget() {
        addCreatureReady(player1, new MinamoSightbender());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void targetsOpponentsCreature() {
        addCreatureReady(player1, new MinamoSightbender());
        Permanent target = addCreatureReady(player2, new AkkiRaider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The targeted creature cannot be declared as blocked")
    void targetedCreatureCannotBeBlocked() {
        addCreatureReady(player1, new MinamoSightbender());
        Permanent attacker = addCreatureReady(player1, new AkkiRaider());
        addCreatureReady(player2, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, attacker.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MinamoSightbender());
        Permanent raider = addCreatureReady(player1, new AkkiRaider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, raider.getId());
        harness.passBothPriorities();
        assertThat(raider.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raider.isCantBeBlocked()).isFalse();
    }
}
