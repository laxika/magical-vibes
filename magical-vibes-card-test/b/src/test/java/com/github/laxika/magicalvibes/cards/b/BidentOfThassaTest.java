package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.s.StormbreathDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BidentOfThassa.class, NessianCourser.class, StormbreathDragon.class})
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
        Permanent ownBear = addCreatureReady(player1, new NessianCourser());
        Permanent enemyBear = addCreatureReady(player2, new NessianCourser());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bident.isTapped()).isTrue();
        assertThat(harness.getAttackLegalityService().getMustAttackRequirementCount(gd, ownBear)).isZero();
        assertThat(harness.getAttackLegalityService().getMustAttackRequirementCount(gd, enemyBear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bident's attack requirement wears off at end of turn")
    void attackRequirementWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BidentOfThassa());
        Permanent enemyBear = addCreatureReady(player2, new NessianCourser());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(enemyBear.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void decliningDrawLeavesHandUnchanged() {
        addBident();
        addReadyAttacker(player1);
        resolveCombatAndTrigger();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void eachCreatureDealingCombatDamageTriggersSeparately() {
        addBident();
        addReadyAttacker(player1);
        addReadyAttacker(player1);
        harness.setLibrary(player1, List.of(new NessianCourser(), new NessianCourser(), new NessianCourser()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void opposingCreatureDealingCombatDamageDoesNotTriggerDraw() {
        addBident();
        addReadyAttacker(player2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void readyOpponentCreatureCannotDeclineToAttack() {
        addBident();
        addCreatureReady(player2, new NessianCourser());
        activateAttackRequirement();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void tappedOpponentCreatureMayStayOutOfCombat() {
        addBident();
        Permanent creature = addCreatureReady(player2, new NessianCourser());
        creature.tap();
        activateAttackRequirement();

        declareAttackers(player2, List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    void hasteCreatureEnteringAfterResolutionMustAttack() {
        addBident();
        harness.forceActivePlayer(player2);
        activateAttackRequirement();
        harness.enterBattlefieldAndReturn(player2, new StormbreathDragon());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void activateAttackRequirement() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void addBident() {
        harness.addToBattlefield(player1, new BidentOfThassa());
    }

    private void addReadyAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = addCreatureReady(player, new NessianCourser());
        attacker.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        harness.setLife(player2, 20);
        resolveCombat();
        harness.passBothPriorities();
    }
}
