package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BidentOfThassaTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control dealing combat damage presents the may-draw choice")
    void combatDamagePresentsMayChoice() {
        addBident();
        addReadyAttacker(player1);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger draws a card")
    void acceptingDrawsCard() {
        addBident();
        addReadyAttacker(player1);

        resolveCombatAndTrigger();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Activating Bident forces only opponents' creatures to attack this turn")
    void forcesOpponentsCreaturesToAttack() {
        Permanent bident = harness.addToBattlefieldAndReturn(player1, new BidentOfThassa());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bident.isTapped()).isTrue();
        assertThat(ownBear.isMustAttackThisTurn()).isFalse();
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Bident's attack requirement wears off at end of turn")
    void attackRequirementWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BidentOfThassa());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enemyBear.isMustAttackThisTurn()).isFalse();
    }

    private void addBident() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new BidentOfThassa()));
    }

    private Permanent addReadyAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }

    private void resolveCombatAndTrigger() {
        harness.setLife(player2, 20);
        resolveCombat();
        harness.passBothPriorities();
    }
}
