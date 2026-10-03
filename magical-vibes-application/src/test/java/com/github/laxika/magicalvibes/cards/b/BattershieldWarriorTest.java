package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattershieldWarrior.class, GrizzlyBears.class})
class BattershieldWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Boast gives your creatures +1/+1 until end of turn")
    void boastBoostsCreaturesYouControl() {
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(3);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(3);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boast requires Battershield Warrior to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new BattershieldWarrior());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(warrior.getEffectivePower()).isEqualTo(3);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(3);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(warrior.getEffectivePower()).isEqualTo(2);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boast affects creatures present at resolution, not creatures entering afterward")
    void boostUsesCreaturesPresentAtResolution() {
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new BattershieldWarrior());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new BattershieldWarrior());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(3);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(3);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boast resolves even if its source leaves the battlefield")
    void boastResolvesWithoutSource() {
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        Permanent remainingCreature = addCreatureReady(player1, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(warrior);
        gd.playerGraveyards.get(player1.getId()).add(warrior.getCard());
        harness.passBothPriorities();

        assertThat(remainingCreature.getEffectivePower()).isEqualTo(3);
        assertThat(remainingCreature.getEffectiveToughness()).isEqualTo(3);
    }
}
