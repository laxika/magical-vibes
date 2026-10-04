package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GoblinFireFiend.class, GrayscaledGharial.class})
class GoblinFireFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Goblin Fire Fiend must be blocked if able")
    void mustBeBlockedIfAble() {
        addAttackingFiend();
        addCreatureReady(player2, new GrayscaledGharial());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("One blocker satisfies Goblin Fire Fiend's requirement")
    void oneBlockerSatisfiesRequirement() {
        addAttackingFiend();
        Permanent blocker = addCreatureReady(player2, new GrayscaledGharial());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Goblin Fire Fiend may remain unblocked when no blocker can block it")
    void noLegalBlockerSatisfiesRequirement() {
        addAttackingFiend();
        Permanent blocker = addCreatureReady(player2, new GrayscaledGharial());
        blocker.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Red mana gives Goblin Fire Fiend +1/+0 until end of turn")
    void activationBoostsSelf() {
        Permanent fiend = addCreatureReady(player1, new GoblinFireFiend());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(1);
        assertThat(fiend.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Goblin Fire Fiend's activation costs one red mana")
    void activationCostsOneRedMana() {
        addCreatureReady(player1, new GoblinFireFiend());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Fire Fiend's activation boost wears off at end of turn")
    void activationBoostResetsAtEndOfTurn() {
        Permanent fiend = addCreatureReady(player1, new GoblinFireFiend());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(fiend.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Goblin Fire Fiend can attack the turn it enters")
    void hasteAllowsImmediateAttack() {
        harness.setHand(player1, List.of(new GoblinFireFiend()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent fiend = findPermanent(player1, "Goblin Fire Fiend");

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(fiend.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations accumulate on only their source")
    void repeatedActivationsBoostOnlyTheirSource() {
        Permanent fiend = addCreatureReady(player1, new GoblinFireFiend());
        Permanent other = addCreatureReady(player1, new GoblinFireFiend());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(2);
        assertThat(fiend.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A blocker cannot ignore the Fiend to block an ordinary attacker")
    void cannotDivertOnlyBlockerToOtherAttacker() {
        addAttackingFiend();
        Permanent other = addCreatureReady(player1, new GrayscaledGharial());
        other.setAttacking(true);
        addCreatureReady(player2, new GrayscaledGharial());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("One available blocker may block either of two Fiends")
    void oneBlockerMayChooseBetweenTwoFiends() {
        addAttackingFiend();
        addAttackingFiend();
        Permanent blocker = addCreatureReady(player2, new GrayscaledGharial());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingFiend() {
        Permanent fiend = addCreatureReady(player1, new GoblinFireFiend());
        fiend.setAttacking(true);
        return fiend;
    }
}
